package com.dietiestate25backend.dao.modelInterface;

public interface UtenteDaoInterface {
    boolean login(String email, String password);
    boolean registrazione(String email, String password);
}
