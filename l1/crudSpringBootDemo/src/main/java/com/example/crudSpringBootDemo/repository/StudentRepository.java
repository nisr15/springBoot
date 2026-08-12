package com.example.crudSpringBootDemo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.crudSpringBootDemo.entity.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>{
    
    // public Student saveStudent(Student studentReq){
    //     //save to db
    //     // System.out.println("Inside Repository");
    //     // Student s1=new Student();
    //     // s1.setAge(23);
    //     // s1.setName("ISR");
    //     // s1.setSubject("Spring Boot");
    //     // s1.setEmail("isr@gmail.com");
    //     // s1.setRollNo(101);

    //     // s1.setAge(studentReq.getAge());
    //     // s1.setName(studentReq.getName());
    //     // s1.setSubject(studentReq.getSubject());
    //     // s1.setEmail(studentReq.getEmail());
    //     // s1.setRollNo(studentReq.getRollNo());

    //     // System.out.println("Existing Repository");
    //     return null;
    // }

    public Optional<Student> findByIdAndDeletedIsFalse(Long id);

    public List<Student> findByDeletedIsFalse();
}
