package com.group12.uno.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cards")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String color;  // RED, BLUE, GREEN, YELLOW, BLACK
    private String value;  // 0-9, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR
    
    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
    
    @ManyToOne
    @JoinColumn(name = "player_id")
    private User player;
    
    private boolean discarded;
} 