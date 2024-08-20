package Backend.BangWool.cosmetics.domain;

import Backend.BangWool.member.domain.MemberEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    @Setter
    @EqualsAndHashCode.Exclude
    private MemberEntity member;

    @Column(nullable = false)
    @Setter
    private String name;


    @Builder
    public LocationEntity(String name) {
        this.name = name;
    }

}
