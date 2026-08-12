package com.example.crudSpringBootDemo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.crudSpringBootDemo.entity.Student;
import com.example.crudSpringBootDemo.service.StudentService;

@RestController   //This tells spring that it is a restcontroller class . it takes rest request and gives response
@RequestMapping("/api/students")
public class StudentController {

    private StudentService studentService;

    public StudentController(StudentService studentService){
        this.studentService=studentService;
    }

    @PostMapping("/")
    public ResponseEntity<Student> createStudent(@RequestBody Student student){
        // System.out.println(student.getName());
        // System.out.println(student.getEmail());
        // System.out.println("Inside Controller");
        Student createdStudent=studentService.createStudent(student);
        // System.out.println("Exiting Controller");
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Student> getStudent(@PathVariable Long id){
        Student studentResp=studentService.getStudent(id);
        if(studentResp==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(studentResp);
    }

    @GetMapping("/")
    public ResponseEntity<List<Student>> getStudents(){
        List<Student> studentList=studentService.getStudents();
        if(studentList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(studentList);
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id,@RequestBody Student student){
        Student studentResp=studentService.updateStudent(id,student);
        if(studentResp==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(studentResp);
    }

    @DeleteMapping("/id/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long id){
        Boolean status=studentService.deleteStudent(id);
        if(!status){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body("Record Deleted");
    }

    @PatchMapping("/id/{id}")
    public ResponseEntity<String> deleteStudentSoft(@PathVariable Long id){
        Boolean status=studentService.deleteStudentSoft(id);
        
        if (!status){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body("Record Deleted softly");
    }

}
