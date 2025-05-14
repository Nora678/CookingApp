# Cooking App

## Overview

**Cooking App** is a Java Swing-based application developed in **2025** by **Nora Paskaleva** as her **Final Informatics Project**.

This application allows users to create, manage, and explore cooking recipes, while also keeping track of their available ingredients and favorite dishes. It features secure authentication and thoughtful UI design with practical functionality for cooking enthusiasts.

---

## 🔑 Main Features

- 🔐 **Secure User Registration and Login**  
  Using BCrypt password hashing for secure credential storage.

- 📖 **Recipe Management**  
  Add, browse, edit, and delete cooking recipes.

- 🧂 **Ingredient Inventory**  
  Keep track of your personal inventory.

- ❤️ **Favorites**  
  Favorite and unfavorite your go-to recipes.

- 🔎 **Advanced Search & Filters**  
  Search, sort, and filter recipes by various criteria.

- 👨‍🍳 **Cookable Recipes Filter**  
  Show only recipes that you can make with ingredients on hand.

- ⏱️ **Session Timeout**  
  Automatic logout after 5 minutes of inactivity for improved security.

---

## 🗂️ Project Structure

| File                 | Description                                           |
|----------------------|-------------------------------------------------------|
| `Main.java`          | Application entry point                               |
| `Login.java`         | Handles user login                                    |
| `Register.java`      | New user registration (with profile image upload)     |
| `Welcome.java`       | Main dashboard (Homepage)                             |
| `AddRecipe.java`     | Form to add new recipes                               |
| `BrowseRecipes.java` | Browse, search, sort, and filter recipes              |
| `RecipeDetails.java` | View and edit individual recipes                      |
| `Favourites.java`    | Manage favorite recipes                               |
| `Inventory.java`     | Manage ingredient inventory                           |
| `Navigator.java`     | Handles window navigation history                     |
| `connect.java`       | Database logic for login and registration             |
| `Session.java`       | Holds session state for logged-in users               |
| `SessionTimer.java`  | Manages automatic logout after inactivity             |
| `UIGlobal.java`      | Centralized UI styling (colors, fonts, icons)         |
| `User.java`          | Represents the user, including profile image          |

---

## 🔒 Security

- Passwords are stored securely using **BCrypt hashing**.
- All database queries use **PreparedStatements** to prevent SQL Injection.

---

## 🕒 Session Management

To enhance security, users are **automatically logged out after 5 minutes of inactivity**.

---
