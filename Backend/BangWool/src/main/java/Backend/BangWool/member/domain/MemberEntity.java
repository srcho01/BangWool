package Backend.BangWool.member.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int memberID;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private LocalDate birth;

    private boolean google;
    private boolean kakao;

    @Builder
    public MemberEntity(String email, String password, String name, String nickname, LocalDate birth) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.birth = birth;
        this.google = false;
        this.kakao = false;
    }

    @Builder
    public MemberEntity(String email, String password, String name, String nickname, LocalDate birth, boolean google, boolean kakao) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.birth = birth;
        this.google = google;
        this.kakao = kakao;
    }
}
