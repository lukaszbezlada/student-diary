package com.lukaszbezlada.studentdiary.service;

import com.lukaszbezlada.studentdiary.entity.*;
import com.lukaszbezlada.studentdiary.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {


    private final ClassTypeRepository classTypeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final UserRoleRepository roleRepository;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, TeacherRepository teacherRepository, StudentRepository studentRepository, UserRoleRepository roleRepository, UserRepository userRepository, ClassTypeRepository classTypeRepository) {
        this.passwordEncoder = passwordEncoder;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.classTypeRepository = classTypeRepository;
    }


    public void addSuitableUserWithEncryptedPassword(UserDTO userDTO) throws Exception {
        passwordEncrypting(userDTO);
        if (userDTO.getClassTypeName() != null && userDTO.getSubject() == null) {
            addStudent(userDTO);
        } else if (userDTO.getSubject() != null) {
            addTeacher(userDTO);
        } else throw new Exception("Nie ma odpowiedniego użytkownika!");
    }

    public void passwordEncrypting(UserDTO userDTO) {
        String passwordHash = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(passwordHash);
    }

    public void addTeacher(UserDTO userDTO) {
        Teacher teacher = createTeacherFromDTO(userDTO);
        teacherRepository.save(teacher);
    }

    public void addStudent(UserDTO userDTO) throws Exception {
        SubjectEnum subject = userDTO.getSubject();
        Student student = createStudentFromDTO(userDTO);
        studentRepository.save(student);
    }

    private Teacher createTeacherFromDTO(UserDTO dto) {
        UserRole teacherRole = roleRepository.findByRole("TEACHER");
        dto.getRoles().add(teacherRole);
        User user = new User(dto.getLogin(), dto.getPassword(), dto.getFirstName(),
                dto.getLastName(), dto.getEmail(), dto.getRoles());
        return new Teacher(null, user, dto.getSubject());
    }

    private Student createStudentFromDTO(UserDTO dto) throws Exception {
        UserRole studentRole = roleRepository.findByRole("STUDENT");
        dto.getRoles().add(studentRole);
        User user = new User(dto.getLogin(), dto.getPassword(), dto.getFirstName(),
                dto.getLastName(), dto.getEmail(), dto.getRoles());
        String classType = String.valueOf(dto.getClassTypeName());
        Optional<ClassType> studentClassTypeOptional = classTypeRepository.findByName(classType);
        if (studentClassTypeOptional.isPresent()) {
            ClassType studentClassType = studentClassTypeOptional.get();
            return new Student(null, user, studentClassType, null, null);
        } else throw new Exception("Nie znaleziono klasy");
    }
}
