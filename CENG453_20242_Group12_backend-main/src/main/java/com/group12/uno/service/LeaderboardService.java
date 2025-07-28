package com.group12.uno.service;

import com.group12.uno.dto.LeaderboardEntry;
import com.group12.uno.model.User; // User modelini import edin
import com.group12.uno.repository.UserRepository; // Sadece UserRepository gerekli
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // UserRepository için constructor otomatik oluşturulur
public class LeaderboardService {

    private final UserRepository userRepository; // Sadece User Repository'ye bağımlılık var

    // Tüm liderlik tabloları için kullanılacak ortak fonksiyon
    private List<LeaderboardEntry> getAllTimeRankedUsers() {
        List<User> users = userRepository.findAll(); // Tüm kullanıcıları veritabanından çek

        return users.stream()
                .map(user -> LeaderboardEntry.builder()
                        .username(user.getUsername())
                        // User entity'sindeki mevcut 'score' alanını kullan
                        .totalScore((long) user.getScore()) // score alanı int ise Long'a cast et
                        // Diğer alanlar (gamesPlayed vb.) User modelinde yoksa kaldırılmalı
                        .build())
                .sorted(Comparator.comparingLong(LeaderboardEntry::getTotalScore).reversed()) // Puana göre büyükten küçüğe sırala
                .collect(Collectors.toList());
    }

    // Haftalık lider tablosu istendiğinde de tüm zamanlar skorunu döndürür
    public List<LeaderboardEntry> getWeeklyLeaderboard() {
        return getAllTimeRankedUsers();
    }

    // Aylık lider tablosu istendiğinde de tüm zamanlar skorunu döndürür
    public List<LeaderboardEntry> getMonthlyLeaderboard() {
        return getAllTimeRankedUsers();
    }

    // Tüm zamanlar lider tablosu istendiğinde de tüm zamanlar skorunu döndürür
    public List<LeaderboardEntry> getAllTimeLeaderboard() {
        return getAllTimeRankedUsers();
    }
}