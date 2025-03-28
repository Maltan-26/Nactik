package com.example.mychatapptutorial;

public class DatabaseConfig {
    // Replace these with your freedatabase.com credentials
    public static final String SUPABASE_URL = "https://fooducmxgzrpitqvskxm.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvb2R1Y214Z3pycGl0cXZza3htIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDMxMDY4MzMsImV4cCI6MjA1ODY4MjgzM30.c52WQuF8FNk9c1BKNK7inIKkcZiJZB2nQroh8FJZKOk";
    public static final String SUPABASE_DATABASE_PASSWORD = "nactik4848";

    // Database connection details
    public static final String DB_URL = "jdbc:postgresql://db.fooducmxgzrpitqvskxm.supabase.co:5432/postgres?user=postgres&password=[nactik4848]";
    public static final String DB_USER = "postgres";
    public static final String DB_PASSWORD = "nactik4848";

    // Connection pool settings
    public static final int INITIAL_POOL_SIZE = 3;
    public static final int MAX_POOL_SIZE = 10;
}