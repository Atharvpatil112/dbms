package com.school.studentrecords;

public record Student(String studentId, String fullName, String email, String course, int yearLevel, String phone) {
    @Override public String toString() {
        return "%s | %s | %s | %s | Year %d | %s".formatted(studentId, fullName, email, course, yearLevel, phone);
    }
}

