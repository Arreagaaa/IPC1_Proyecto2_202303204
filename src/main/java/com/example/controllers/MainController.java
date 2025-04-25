package com.example.controllers;

import com.example.views.LoginView;
import com.example.utils.Serializador;

public class MainController {

    public void run() {
        // Inicializa la aplicación mostrando la vista de login
        LoginView loginView = new LoginView();
        loginView.setVisible(true);
    }
}