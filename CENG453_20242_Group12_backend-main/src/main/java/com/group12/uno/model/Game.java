package com.group12.uno.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String gameName;

    @Column(nullable = false)
    private Integer maxPlayers;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private Set<GamePlayer> players = new HashSet<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    private List<Card> cards = new ArrayList<>();

    private String currentColor;
    private String direction = "CLOCKWISE";

    @ManyToOne
    @JoinColumn(name = "current_player_id")
    private User currentPlayer;

    public enum GameStatus {
        WAITING,
        IN_PROGRESS,
        FINISHED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 