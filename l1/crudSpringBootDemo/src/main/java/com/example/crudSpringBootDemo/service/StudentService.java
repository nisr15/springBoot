package com.example.crudSpringBootDemo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.crudSpringBootDemo.entity.Student;
import com.example.crudSpringBootDemo.repository.StudentRepository;

@Service
public class StudentService {

    private StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository){
        this.studentRepository=studentRepository;
    }

    public Student createStudent(Student studentReq){
        //business logic
        // System.out.println("Inside Service");
        studentReq.setDeleted(false);
        Student studentResp=studentRepository.save(studentReq);
        // System.out.println("Existing Service");
        return studentResp;
    }

    public Student getStudent(Long id){
        Optional<Student> studentResp=studentRepository.findByIdAndDeletedIsFalse(id);
        if(studentResp.isPresent()){
            return studentResp.get();
        }
        return null;
        
    }

    public List<Student> getStudents(){
        List<Student> studentResp=studentRepository.findByDeletedIsFalse();
        if(studentResp.isEmpty()){
            return null;
        }
        return studentResp;
        
        
    }

    public Student updateStudent(Long id,Student student){
        Optional<Student> studentResp=studentRepository.findById(id);
        if(studentResp.isPresent()){
            Student studentToSave=studentResp.get();
            studentToSave.setName(student.getName());
            studentToSave.setRollNo(student.getRollNo());
            studentToSave.setEmail(student.getEmail());
            studentToSave.setAge(student.getAge());
            studentToSave.setSubject(student.getSubject());
            studentToSave.setDeleted(false);
            studentRepository.save(studentToSave);

            return studentResp.get();
        }
        return null;
        
    }


    public Boolean deleteStudent(Long id){
        Boolean status=studentRepository.existsById(id);
        if(!status){
            return false;
        }
        studentRepository.deleteById(id);
        return true;
    } 

    public Boolean deleteStudentSoft(Long id){
        Optional<Student> existingStudent=studentRepository.findByIdAndDeletedIsFalse(id);
        if(existingStudent.isEmpty()){
            return false;
        }
        
        Student studentToSave=existingStudent.get();
        studentToSave.setDeleted(true);
        studentRepository.save(studentToSave);
        return true;
    }
}
