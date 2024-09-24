package Backend.BangWool.cosmetics.domain;

import Backend.BangWool.exception.ServerException;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.util.CONSTANT;
import jakarta.persistence.*;
import lombok.*;

import java.net.URI;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id", "member"})
public class CosmeticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    @Setter
    private MemberEntity member;


    @Column(nullable = false)
    @Setter
    private String name;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Setter
    private LocalDate startDate;

    @Column(nullable = false)
    private int status;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    @Setter
    private LocationEntity location;

    @Column(nullable = false)
    @Setter
    private URI image;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Setter
    private Category category;


    @Builder
    public CosmeticsEntity(String name, LocalDate expirationDate, Category category, LocationEntity location, URI image) {
        this.name = name;
        this.expirationDate = expirationDate;
        this.category = category;
        this.location = location;
        this.image = image == null ? getDefaultImage(category) : image;
        this.status = 0;
    }

    public static URI getDefaultImage(Category category) {
        return switch (category) {
            case basic -> CONSTANT.DEFAULT_COS_BASIC;
            case base -> CONSTANT.DEFAULT_COS_BASE;
            case color -> CONSTANT.DEFAULT_COS_COLOR;
            case others -> CONSTANT.DEFAULT_COS_OTHERS;
        };
    }


    public void setStatus(int status) {
        if (this.status <= status) {
            this.status = status;
        } else {
            throw new ServerException("Status can only proceed");
        }
    }

}
