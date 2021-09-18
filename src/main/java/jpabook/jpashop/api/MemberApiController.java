package jpabook.jpashop.api;


import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

// Controller + ResponseBody를 합친 annotation
@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    // 엔티티에 화면에 뿌려지기 위한 로직이 포함되면 굉장히 복잡해진다.
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect);
    }


    // 1차의 문제, 데이터가 없어도 null 로 들어가버린다.
    // Member 엔티티의 name에 @NotEmpty를 넣으면 null은 못들어간다.
    // 문제점 : 엔티티의 필드 이름을 변경했다? 엔티티와 API의 스펙이 1대1로 매핑되면 안된다.
    // API를 위한 별도의 데이터 transobject 즉, DTO와 같은 것을 구현해야된다.
    // 엔티티를 외부에 노출하면 안된다. 엔티티를 파라미터로 받지 말아라
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    // 만약, 엔티티의 수정이 있어도, compile 단계에서 오류가 난다.
    // 별도의 DTO로 API를 받는 것이 정석이라고 할 수 있다.
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {

        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }


    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName());
        // Member 엔티티의 update 메소드에서 Member 를 반환할 수도 있겠지만 (각자의 스타일이 있을 것이다)
        // Controller 쪽에서 find 메소드로 다시 객체를 찾고 이를 반환해주는 것이 유지보수가 더 좋다.
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }


    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    // 바로 json의 배열 타입으로 return 하면, 유연성이 확 떨어지기 때문에
    // 해당 과정처럼 한 번 더 감싸줘야한다.
    @Data
    @AllArgsConstructor
    private class Result<T> {
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }
}
