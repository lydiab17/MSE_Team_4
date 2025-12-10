package com.evote.app.citizen_management.application.dto;

public class CitizenDto {
    private String userId;
    private String vorname;
    private String nachname;
    private String email;
    private String password;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String vorname) {
        this.nachname = nachname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public CitizenDto(String userId, String vorname, String nachname, String email, String password) {
        this.userId = userId;
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.password = password;
    }
}
