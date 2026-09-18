package com.school.studentrecords;

import java.sql.*;
import java.util.*;

public final class MySqlStudentRepository implements StudentRepository {
    private final Connection connection;
    public MySqlStudentRepository(AppConfig config) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(config.get("mysql.url"), config.get("mysql.user"), config.get("mysql.password"));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver is missing. Run Maven again so it downloads project dependencies.", e);
        } catch (SQLException e) { throw new IllegalStateException("Cannot connect to MySQL: " + e.getMessage(), e); }
    }
    public void save(Student s) {
        String sql = "INSERT INTO students (student_id, full_name, email, course, year_level, phone) VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE full_name=VALUES(full_name), email=VALUES(email), course=VALUES(course), year_level=VALUES(year_level), phone=VALUES(phone)";
        try (PreparedStatement p = connection.prepareStatement(sql)) { bind(p, s); p.executeUpdate(); } catch (SQLException e) { throw new IllegalStateException("Could not save student: " + e.getMessage(), e); }
    }
    public Optional<Student> findById(String id) {
        try (PreparedStatement p = connection.prepareStatement("SELECT * FROM students WHERE student_id=?")) { p.setString(1,id); try (ResultSet r=p.executeQuery()) { return r.next()?Optional.of(map(r)):Optional.empty(); } } catch(SQLException e){ throw new IllegalStateException(e); }
    }
    public List<Student> findAll() {
        List<Student> result=new ArrayList<>(); try(Statement s=connection.createStatement(); ResultSet r=s.executeQuery("SELECT * FROM students ORDER BY student_id")){while(r.next())result.add(map(r));return result;}catch(SQLException e){throw new IllegalStateException(e);}
    }
    public boolean deleteById(String id) { try(PreparedStatement p=connection.prepareStatement("DELETE FROM students WHERE student_id=?")){p.setString(1,id);return p.executeUpdate()>0;}catch(SQLException e){throw new IllegalStateException(e);} }
    private void bind(PreparedStatement p, Student s)throws SQLException{p.setString(1,s.studentId());p.setString(2,s.fullName());p.setString(3,s.email());p.setString(4,s.course());p.setInt(5,s.yearLevel());p.setString(6,s.phone());}
    private Student map(ResultSet r)throws SQLException{return new Student(r.getString("student_id"),r.getString("full_name"),r.getString("email"),r.getString("course"),r.getInt("year_level"),r.getString("phone"));}
    public String databaseName(){return "MySQL (XAMPP/phpMyAdmin)";} public void close(){try{connection.close();}catch(SQLException ignored){}}
}

