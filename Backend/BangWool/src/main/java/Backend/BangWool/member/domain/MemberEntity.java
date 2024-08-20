package Backend.BangWool.member.domain;

import Backend.BangWool.cosmetics.domain.CosmeticsEntity;
import Backend.BangWool.cosmetics.domain.LocationEntity;
import Backend.BangWool.util.CONSTANT;
import jakarta.persistence.*;
import lombok.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Setter
    private String password;

    @Column(nullable = false)
    private String name;

    @Setter
    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private LocalDate birth;

    @Setter
    private String googleId;
    @Setter
    private String kakaoId;

    @Setter
    private URI profileImage;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CosmeticsEntity> cosmetics;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocationEntity> locationOptions;


    @Builder
    public MemberEntity(String email, String password, String name, String nickname, LocalDate birth, String googleId, String kakaoId) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.birth = birth;
        this.googleId = googleId;
        this.kakaoId = kakaoId;

        this.profileImage = CONSTANT.DEFAULT_PROFILE;

        this.cosmetics = new ArrayList<>();
        this.locationOptions = new ArrayList<>();
    }

    public void addCosmetics(CosmeticsEntity cosmetics) {
        this.cosmetics.add(cosmetics);
        cosmetics.setMember(this);
    }

    public void removeCosmetics(CosmeticsEntity cosmetics) {
        this.cosmetics.remove(cosmetics);
    }

    public void addLocation(LocationEntity location) {
        this.locationOptions.add(location);
        location.setMember(this);
    }

    public void removeLocation(LocationEntity location) {
        this.locationOptions.remove(location);
    }

}
