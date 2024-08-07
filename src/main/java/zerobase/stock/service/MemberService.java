package zerobase.stock.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.stock.exception.impl.AlreadyExistUserException;
import zerobase.stock.model.Auth;
import zerobase.stock.persist.MemberRepository;
import zerobase.stock.persist.entity.MemberEntity;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user: " + username));
    }

    public MemberEntity register(Auth.SignUp member) {

        boolean exists = memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new AlreadyExistUserException();
        }

        member.setPassword(passwordEncoder.encode(member.getPassword()));

        return memberRepository.save(member.toEntity());
    }

    public MemberEntity authenticate(Auth.SignIn member) {
        MemberEntity user = memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 ID 입니다."));

        if (!passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

}
