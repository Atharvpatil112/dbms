package com.school.studentrecords;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends AutoCloseable {
    void save(Student student);
    Optional<Student> findById(String studentId);
    List<Student> findAll();
    boolean deleteById(String studentId);
    String databaseName();
    @Override default void close() {}
}

