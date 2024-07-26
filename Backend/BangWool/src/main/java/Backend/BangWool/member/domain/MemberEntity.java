package Backend.BangWool.member.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int memberID;

    @Column(unique = true, nullable = false)
    private String email;

    @Setter
    private String password;

    @Column(nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private LocalDate birth;

    private String googleId;
    private String kakaoId;

    @Builder
    public MemberEntity(String email, String password, String name, String nickname, LocalDate birth, String googleId, String kakaoId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.birth = birth;
        this.googleId = googleId;
        this.kakaoId = kakaoId;
    }

}
