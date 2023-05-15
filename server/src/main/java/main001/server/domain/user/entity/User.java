package main001.server.domain.user.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import main001.server.audit.BaseTimeEntity;
import main001.server.domain.portfolio.entity.Portfolio;
import main001.server.domain.portfoliocomment.entity.PortfolioComment;
import main001.server.domain.skill.entity.UserSkill;
import main001.server.domain.user.enums.Grade;
import main001.server.domain.user.enums.JobStatus;
import main001.server.domain.user.enums.UserStatus;
import main001.server.domain.usercomment.entity.UserComment;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(length = 50, unique = true)
    private String email;

    @Column(length = 20)
    private String name;

    @Column(length = 100)
    private String password;

    @Column
    private String profileImg;

    @Column(length = 50)
    private String gitLink;

    @Column(length = 60)
    private String blogLink;

    @Enumerated(value = EnumType.STRING)
    @Column(length = 20)
    private Grade grade = Grade.NOVICE;

    @Enumerated(value = EnumType.STRING)
    private JobStatus jobStatus;

    @Column(length = 500)
    private String about;

    @Enumerated(value = EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UserStatus userStatus = UserStatus.USER_ACTIVE;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>(); // ROLE_USER<DEFAULT>, ADMIN

    private boolean auth = false;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Portfolio> portfolios = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<UserComment> userComments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<PortfolioComment> portfolioComments = new ArrayList<>();

    public User(Long userId, String email, String name, String profileImg, String gitLink, String blogLink, JobStatus jobStatus, String about) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.profileImg = profileImg;
        this.gitLink = gitLink;
        this.blogLink = blogLink;
        this.jobStatus = jobStatus;
        this.about = about;
    }

    public User(String email, String name, String profileImg) {
        this.email = email;
        this.name = name;
        this.profileImg = profileImg;
    }

    public void addSkill(UserSkill userSkill) {
        skills.add(userSkill);
        userSkill.setUser(this);
    }
}
