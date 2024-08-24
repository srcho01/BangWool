package Backend.BangWool.cosmetics.service;

import Backend.BangWool.cosmetics.domain.CosmeticsEntity;
import Backend.BangWool.cosmetics.domain.LocationEntity;
import Backend.BangWool.cosmetics.dto.LocationUpdateRequest;
import Backend.BangWool.cosmetics.repository.LocationRepository;
import Backend.BangWool.exception.BadRequestException;
import Backend.BangWool.exception.NotFoundException;
import Backend.BangWool.exception.ServerException;
import Backend.BangWool.member.domain.MemberEntity;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final MemberRepository memberRepository;
    private final LocationRepository locationRepository;


    public LocationEntity create(Session session, String name) {
        // 위치 이름 길이 체크
        validateLocationName(name);

        // 부모 가져오기
        MemberEntity parent = getMember(session.getId());

        // 이미 존재하는 옵션인지 체크
        if (locationRepository.existsByMemberAndName(parent, name)) {
            return locationRepository.findByMemberAndName(parent, name)
                    .orElseThrow(() -> new ServerException("Invalid location"));
        }

        // 옵션 생성
        LocationEntity location = LocationEntity.builder().name(name).build();
        parent.addLocation(location);

        // 옵션 DB 업데이트
        LocationEntity saved = locationRepository.save(location);
        memberRepository.save(parent);

        return saved;
    }

    public List<String> read(Session session) {
        MemberEntity member = getMember(session.getId());

        List<String> locations = new ArrayList<>();
        for (LocationEntity loc: member.getLocationOptions()) {
            locations.add(loc.getName());
        }

        return locations;
    }

    public List<String> update(Session session, LocationUpdateRequest request) {
        // 변경 옵션 목록 가져오기
        Map<String, String> updateOptions = request.getOptions();

        // null이라면 변경 없이 목록 return
        if (updateOptions == null) {
            return read(session);
        }

        // 해당 유저의 옵션 목록 조회
        MemberEntity member = getMember(session.getId());
        List<LocationEntity> locationOptions = member.getLocationOptions();

        // 옵션 변경
        updateLocationNames(locationOptions, updateOptions);

        // 리포지토리에 업데이트
        locationRepository.saveAll(member.getLocationOptions());
        memberRepository.save(member);

        return read(session);
    }

    public boolean delete(Session session, String updateOptionName) {
        MemberEntity member = getMember(session.getId());

        // member의 위치 옵션들 중, updateOptionName과 같은 LocationEntity
        LocationEntity location = member.getLocationOptions().stream()
                .filter(loc -> loc.getName().equals(updateOptionName))
                .findFirst().orElse(null);
        if (location == null) {
            return false;
        }

        // cosmetics의 참조가 남아있다면 삭제 불가
        for (CosmeticsEntity cosmetics: member.getCosmetics()) {
            if (updateOptionName.equals(cosmetics.getName())) {
                return false;
            }
        }

        // 참조하는 cosmetics 수가 0이므로 삭제
        member.removeLocation(location);
        memberRepository.save(member);
        locationRepository.delete(location);

        return true;
    }


    private void validateLocationName(String name) {
        if (name.isEmpty() || name.length() > 10) {
            throw new BadRequestException("Location option \"" + name + "\" should be between 1 and 10 characters");
        }
    }

    private MemberEntity getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void updateLocationNames(List<LocationEntity> locationOptions, Map<String, String> updateOptions) {
        for (LocationEntity locationEntity: locationOptions) {
            if (updateOptions.containsKey(locationEntity.getName())) {
                String newName = updateOptions.get(locationEntity.getName());
                validateLocationName(newName);
                locationEntity.setName(newName);
            }
        }
    }

}
