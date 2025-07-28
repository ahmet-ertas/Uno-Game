package com.group12.uno.service;

import com.group12.uno.dto.CreateGameRequest;
import com.group12.uno.dto.GameResponse;
import com.group12.uno.model.*;
import com.group12.uno.repository.GamePlayerRepository;
import com.group12.uno.repository.GameRepository;
import com.group12.uno.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private static final String[] COLORS = {"RED", "BLUE", "GREEN", "YELLOW"};
    private static final String[] VALUES = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "SKIP", "REVERSE", "DRAW_TWO"};
    private static final String[] WILD_CARDS = {"WILD", "WILD_DRAW_FOUR"};

    @Transactional
    public GameResponse createGame(CreateGameRequest request, String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Game game = Game.builder()
                .gameName(request.getGameName())
                .maxPlayers(request.getMaxPlayers())
                .status(Game.GameStatus.WAITING)
                .creator(creator)
                .build();
        game = gameRepository.save(game);

        GamePlayer gamePlayer = GamePlayer.builder()
                .game(game)
                .player(creator)
                .score(0)
                .build();
        gamePlayerRepository.save(gamePlayer);

        return mapToGameResponse(game);
    }

    @Transactional
    public GameResponse joinGame(Long gameId, String username) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        if (game.getStatus() != Game.GameStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is not in waiting state");
        }

        if (game.getPlayers().size() >= game.getMaxPlayers()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is full");
        }

        User player = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (game.getPlayers().stream().anyMatch(gp -> gp.getPlayer().equals(player))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player already in game");
        }

        GamePlayer gamePlayer = GamePlayer.builder()
                .game(game)
                .player(player)
                .score(0)
                .build();
        gamePlayerRepository.save(gamePlayer);

        return mapToGameResponse(game);
    }

    @Transactional
    public GameResponse startGame(Long gameId, String username) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        if (!game.getCreator().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator can start the game");
        }

        if (game.getStatus() != Game.GameStatus.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is not in waiting state");
        }

        if (game.getPlayers().size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough players to start the game");
        }

        game.setStatus(Game.GameStatus.IN_PROGRESS);
        game = gameRepository.save(game);

        return mapToGameResponse(game);
    }

    public GameResponse getGameState(Long gameId, String username) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));

        if (game.getPlayers().stream().noneMatch(gp -> gp.getPlayer().getUsername().equals(username))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a player in this game");
        }

        return mapToGameResponse(game);
    }

    private GameResponse mapToGameResponse(Game game) {
        List<GameResponse.PlayerScore> playerScores = game.getPlayers().stream()
                .map(gp -> new GameResponse.PlayerScore(gp.getPlayer().getUsername(), gp.getScore()))
                .collect(Collectors.toList());

        return GameResponse.builder()
                .id(game.getId())
                .gameName(game.getGameName())
                .maxPlayers(game.getMaxPlayers())
                .currentPlayers(game.getPlayers().size())
                .status(game.getStatus())
                .creatorUsername(game.getCreator().getUsername())
                .createdAt(game.getCreatedAt())
                .players(playerScores)
                .build();
    }

    private void dealCards(Game game) {
        List<Card> deck = game.getCards();
        List<User> players = game.getPlayers().stream()
                .map(GamePlayer::getPlayer)
                .collect(Collectors.toList());
        
        for (User player : players) {
            List<Card> playerCards = deck.stream()
                    .filter(card -> !card.isDiscarded())
                    .limit(7)
                    .collect(Collectors.toList());
            
            playerCards.forEach(card -> {
                card.setPlayer(player);
                card.setGame(game);
            });
            
            deck.removeAll(playerCards);
        }
        
        // Set the first card
        Card firstCard = deck.get(0);
        firstCard.setDiscarded(true);
        game.setCurrentColor(firstCard.getColor());
    }

    @Transactional
    public Game playCard(Long gameId, Long cardId, User player) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        
        if (game.getStatus() != Game.GameStatus.IN_PROGRESS) {
            throw new RuntimeException("Game is not in progress");
        }
        
        if (!game.getCurrentPlayer().equals(player)) {
            throw new RuntimeException("Not your turn");
        }
        
        Card card = game.getCards().stream()
                .filter(c -> c.getId().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Card not found"));
        
        if (!isValidMove(game, card)) {
            throw new RuntimeException("Invalid move");
        }
        
        executeCardAction(game, card);
        return gameRepository.save(game);
    }

    private boolean isValidMove(Game game, Card card) {
        Card topCard = game.getCards().stream()
                .filter(Card::isDiscarded)
                .reduce((first, second) -> second)
                .orElseThrow();
        
        return card.getColor().equals("BLACK") ||
                card.getColor().equals(game.getCurrentColor()) ||
                card.getValue().equals(topCard.getValue());
    }

    private void executeCardAction(Game game, Card card) {
        card.setDiscarded(true);
        game.setCurrentColor(card.getColor());
        
        List<User> players = game.getPlayers().stream()
                .map(GamePlayer::getPlayer)
                .collect(Collectors.toList());
        int currentPlayerIndex = players.indexOf(game.getCurrentPlayer());
        int nextPlayerIndex;
        
        switch (card.getValue()) {
            case "SKIP":
                nextPlayerIndex = getNextPlayerIndex(game, currentPlayerIndex, 2);
                break;
            case "REVERSE":
                game.setDirection(game.getDirection().equals("CLOCKWISE") ? "COUNTER_CLOCKWISE" : "CLOCKWISE");
                nextPlayerIndex = getNextPlayerIndex(game, currentPlayerIndex, 1);
                break;
            case "DRAW_TWO":
                nextPlayerIndex = getNextPlayerIndex(game, currentPlayerIndex, 1);
                User nextPlayer = players.get(nextPlayerIndex);
                drawCards(game, nextPlayer, 2);
                nextPlayerIndex = getNextPlayerIndex(game, nextPlayerIndex, 1);
                break;
            case "WILD_DRAW_FOUR":
                nextPlayerIndex = getNextPlayerIndex(game, currentPlayerIndex, 1);
                nextPlayer = players.get(nextPlayerIndex);
                drawCards(game, nextPlayer, 4);
                nextPlayerIndex = getNextPlayerIndex(game, nextPlayerIndex, 1);
                break;
            default:
                nextPlayerIndex = getNextPlayerIndex(game, currentPlayerIndex, 1);
        }
        
        game.setCurrentPlayer(players.get(nextPlayerIndex));
    }

    private int getNextPlayerIndex(Game game, int currentIndex, int steps) {
        int playerCount = game.getPlayers().size();
        if (game.getDirection().equals("CLOCKWISE")) {
            return (currentIndex + steps) % playerCount;
        } else {
            return (currentIndex - steps + playerCount) % playerCount;
        }
    }

    private void drawCards(Game game, User player, int count) {
        List<Card> availableCards = game.getCards().stream()
                .filter(card -> !card.isDiscarded() && card.getPlayer() == null)
                .collect(Collectors.toList());
        
        List<Card> drawnCards = availableCards.stream()
                .limit(count)
                .collect(Collectors.toList());
        
        drawnCards.forEach(card -> {
            card.setPlayer(player);
            card.setGame(game);
        });
    }
} 