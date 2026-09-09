#!/bin/bash

# Electricity Bill Generation System Build & Launch Script

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPT_DIR"

echo "=========================================================="
echo "⚡ ELECTRICITY BILL GENERATION SYSTEM - BUILD & LAUNCH"
echo "=========================================================="

LIB_DIR="$SCRIPT_DIR/lib"
BIN_DIR="$SCRIPT_DIR/bin"

mkdir -p "$LIB_DIR"
mkdir -p "$BIN_DIR"

# 1. Download MySQL Connector JAR if missing
MYSQL_JAR="$LIB_DIR/mysql-connector-j-8.3.0.jar"
if [ ! -f "$MYSQL_JAR" ]; then
    echo "Downloading MySQL Connector/J 8.3.0 driver..."
    curl -sSL "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar" -o "$MYSQL_JAR" || true
fi

# 2. Download SQLite JDBC Driver JAR if missing
SQLITE_JAR="$LIB_DIR/sqlite-jdbc-3.45.1.0.jar"
if [ ! -f "$SQLITE_JAR" ]; then
    echo "Downloading SQLite JDBC 3.45.1.0 driver for fallback mode..."
    curl -sSL "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar" -o "$SQLITE_JAR" || true
fi

# 3. Download SLF4J dependencies if missing
SLF4J_API="$LIB_DIR/slf4j-api-2.0.9.jar"
if [ ! -f "$SLF4J_API" ]; then
    curl -sSL "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar" -o "$SLF4J_API" || true
fi

SLF4J_SIMPLE="$LIB_DIR/slf4j-simple-2.0.9.jar"
if [ ! -f "$SLF4J_SIMPLE" ]; then
    curl -sSL "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.9/slf4j-simple-2.0.9.jar" -o "$SLF4J_SIMPLE" || true
fi

# 4. Download PDFBox & Commons Logging dependencies if missing
PDFBOX_JAR="$LIB_DIR/pdfbox-2.0.29.jar"
if [ ! -f "$PDFBOX_JAR" ]; then
    curl -sSL "https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/2.0.29/pdfbox-2.0.29.jar" -o "$PDFBOX_JAR" || true
fi

FONTBOX_JAR="$LIB_DIR/fontbox-2.0.29.jar"
if [ ! -f "$FONTBOX_JAR" ]; then
    curl -sSL "https://repo1.maven.org/maven2/org/apache/pdfbox/fontbox/2.0.29/fontbox-2.0.29.jar" -o "$FONTBOX_JAR" || true
fi

COMMONS_LOGGING="$LIB_DIR/commons-logging-1.2.jar"
if [ ! -f "$COMMONS_LOGGING" ]; then
    curl -sSL "https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar" -o "$COMMONS_LOGGING" || true
fi

echo "Compiling Java source files..."
JAVA_FILES=$(find src -name "*.java")

javac -cp "lib/*" -d bin $JAVA_FILES

echo "Compilation successful! Launching Electricity Bill Generation System..."
echo "----------------------------------------------------------"

java -cp "lib/*:bin" Main
