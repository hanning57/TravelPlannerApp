package com.travelplanner.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Landmark {

    public static List<String> getSuggestions(String city) {
        String cityName = city.toUpperCase();
        if (cityName == null || cityName.isEmpty()) {
            return new ArrayList<>();
        }
        
        
        switch (cityName) {
            case "KUALA LUMPUR" -> {
                return Arrays.asList("Petronas Twin Tower", "Merdeka Square", "Istana Negara");
            }
            case "PENANG", "GEORGETOWN" -> {
                return Arrays.asList("Penang Hill Bukit Bendera", "Lee Jetty", "Queen Victoria Memorial Clock Tower", "Kek Lok Si Temple", "Penang Street Art", "Fort Cornwallis");
            }
            case "MELAKA" -> {
                return Arrays.asList("A Famosa", "Church of Saint Paul", "Stadthuys");
            } 
            case "LANGKAWI" -> {
                return Arrays.asList("Langkawi Sky Bridge", "Eagle Square (Dataran Lang)", "Underwater World Langkawi");
            }
            case "KOTA KINABALU" -> {
                return Arrays.asList("Mount Kinabalu", "Tunku Abdul Rahman Park", "KK City Mosque");
            }
            case "SINGAPORE" -> {
                return Arrays.asList("Marina Bay Sands", "Gardens by the Bay", "Sentosa Island", "Merlion Park");
            }
            case "BANGKOK" -> {
                return Arrays.asList("Grand Palace", "Wat Arun", "Chatuchak Weekend Market", "Lumphini Park");
            }
            case "BALI" -> {
                return Arrays.asList("Uluwatu Temple", "Tegallalang Rice Terrace", "Sacred Monkey Forest Sanctuary");
            }
            case "HANOI" -> {
                return Arrays.asList("Ha Long Bay", "Old Quarter", "Ho Chi Minh Mausoleum");
            }
            case "SEOUL" -> {
                return Arrays.asList("Gyeongbokgung Palace", "N Seoul Tower", "Bukchon Hanok Village", "Myeong-dong");
            }
            case "HONG KONG" -> {
                return Arrays.asList("Victoria Peak", "Tian Tan Buddha", "Star Ferry", "Hong Kong Disneyland");
            }
            case "PARIS" -> {
                return Arrays.asList("Eiffel Tower", "Louvre Museum", "Notre Dame");
            } 
            case "TOKYO" -> {
                return Arrays.asList("Shibuya Crossing", "Tokyo Tower", "Senso-ji Temple");
            }     
            case "LONDON" -> {
                return Arrays.asList("Big Ben", "London Eye", "Tower of London");
            }

            default -> {
                return new ArrayList<>();
            } 
        }
    }
}

