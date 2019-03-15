package Util;

public class Triple {
    private String subject, predicate, object;

    public Triple(String subject, String predicate, String object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    @Override
    public boolean equals(Object obj) {
        Triple triple = (Triple) obj;
        return subject.equals(triple.subject) && predicate.equals(triple.predicate) && object.equals(triple.object);
    }

    @Override
    public String toString() {
        return "(" + subject + ", " + predicate + ", " + object + ")";
    }

    public String getSubject() {
        return subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }
}