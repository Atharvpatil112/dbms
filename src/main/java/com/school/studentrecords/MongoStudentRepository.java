package com.school.studentrecords;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import java.util.*;

public final class MongoStudentRepository implements StudentRepository {
    private final MongoClient client;
    private final MongoCollection<Document> students;
    public MongoStudentRepository(AppConfig config) {
        client = MongoClients.create(config.get("mongodb.uri"));
        students = client.getDatabase(config.get("mongodb.database")).getCollection("students");
        students.createIndex(new Document("studentId", 1));
    }
    public void save(Student s) { students.replaceOne(Filters.eq("studentId", s.studentId()), toDocument(s), new ReplaceOptions().upsert(true)); }
    public Optional<Student> findById(String id) { Document d=students.find(Filters.eq("studentId",id)).first(); return Optional.ofNullable(d).map(this::fromDocument); }
    public List<Student> findAll() { List<Student> result=new ArrayList<>(); for(Document d:students.find().sort(new Document("studentId",1)))result.add(fromDocument(d)); return result; }
    public boolean deleteById(String id) { return students.deleteOne(Filters.eq("studentId",id)).getDeletedCount()>0; }
    private Document toDocument(Student s) { return new Document("studentId",s.studentId()).append("fullName",s.fullName()).append("email",s.email()).append("course",s.course()).append("yearLevel",s.yearLevel()).append("phone",s.phone()); }
    private Student fromDocument(Document d) { return new Student(d.getString("studentId"),d.getString("fullName"),d.getString("email"),d.getString("course"),d.getInteger("yearLevel"),d.getString("phone")); }
    public String databaseName(){return "MongoDB (view in Compass)";} public void close(){client.close();}
}

