package Backend.BangWool.cosmetics.domain;

import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.util.CONSTANT;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URI;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class CosmeticsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    @Setter
    private MemberEntity member;


    @Column(nullable = false)
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
    private URI image;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;
    
    public enum Category {
        basic, base, color, others
    }


    @Builder
    public CosmeticsEntity(String name, LocalDate expirationDate, Category category, LocationEntity location) {
        this(name, expirationDate, category, location, getDefaultImage(category));
    }

    @Builder
    public CosmeticsEntity(String name, LocalDate expirationDate, Category category, LocationEntity location, URI image) {
        this.name = name;
        this.expirationDate = expirationDate;
        this.category = category;
        this.location = location;
        this.image = image;
        this.status = 0;
    }

    private static URI getDefaultImage(Category category) {
        return switch (category) {
            case basic -> CONSTANT.DEFAULT_COS_BASIC;
            case base -> CONSTANT.DEFAULT_COS_BASE;
            case color -> CONSTANT.DEFAULT_COS_COLOR;
            case others -> CONSTANT.DEFAULT_COS_OTHERS;
        };
    }

}
