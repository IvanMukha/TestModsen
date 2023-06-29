package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrencyCalculator {
    private static double conversionRate;

    public static void main(String[] args) {
        loadConversionRate();

        String input = "toDollars(737p + toRubles($85.4))";
        double result = evaluateExpression(input);
        System.out.printf("Result: $%.2f\n", result);
    }

    private static double evaluateExpression(String expression) {
        String dollarPattern = "\\$[\\d.,]+";
        String rublePattern = "[\\d.,]+p";

        // Convert dollars to rubles
        Pattern pattern = Pattern.compile(dollarPattern);
        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            String match = matcher.group();
            double amount = parseAmount(match.substring(1), "$");
            double convertedAmount = convertDollarsToRubles(amount);
            expression = expression.replace(match, String.valueOf(convertedAmount));
        }

        // Convert rubles to dollars
        pattern = Pattern.compile(rublePattern);
        matcher = pattern.matcher(expression);
        while (matcher.find()) {
            String match = matcher.group();
            double amount = parseAmount(match.substring(0, match.length() - 1), "p");
            double convertedAmount = convertRublesToDollars(amount);
            expression = expression.replace(match, String.valueOf(convertedAmount));
        }

        // Evaluate the expression
        return evaluate(expression);
    }

    private static double parseAmount(String amount, String currencySymbol) {
        amount = amount.replaceAll(",", ""); // Remove commas from the amount

        if (amount.startsWith(currencySymbol)) {
            amount = amount.substring(currencySymbol.length()); // Remove the currency symbol from the start
        } else if (amount.endsWith(currencySymbol)) {
            amount = amount.substring(0, amount.length() - currencySymbol.length()); // Remove the currency symbol from the end
        }

        return Double.parseDouble(amount);
    }

    private static double evaluate(String expression) {
        expression = expression.replaceAll("\\s", ""); // Remove whitespace

        // Base case: single number
        if (expression.matches("^[-+]?[0-9]*\\.?[0-9]+$")) {
            return Double.parseDouble(expression);
        }

        // Check if the expression is in the form of "toDollars or "toRubles
        if (expression.startsWith("toDollars(") || expression.startsWith("toRubles(")) {
            int openBracketIndex = expression.indexOf("(");
            int closeBracketIndex = expression.lastIndexOf(")");

            if (openBracketIndex == -1 || closeBracketIndex == -1) {
                throw new RuntimeException("Invalid expression: " + expression);
            }

            String insideBracket = expression.substring(openBracketIndex + 1, closeBracketIndex);
            double result = evaluateExpression(insideBracket);

            if (expression.startsWith("toDollars(")) {
                return convertRublesToDollars(result);
            } else {
                return convertDollarsToRubles(result);
            }
        }

        // Evaluate addition and subtraction
        double total = 0;
        int startIndex = 0;
        boolean addition = true;

        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '+') {
                if (addition) {
                    String term = expression.substring(startIndex, i);
                    double value = evaluate(term);
                    total += value;
                } else {
                    String term = expression.substring(startIndex, i);
                    double value = evaluate(term);
                    total -= value;
                }

                startIndex = i + 1;
                addition = true;
            } else if (expression.charAt(i) == '-') {
                if (addition) {
                    String term = expression.substring(startIndex, i);
                    double value = evaluate(term);
                    total += value;
                } else {
                    String term = expression.substring(startIndex, i);
                    double value = evaluate(term);
                    total -= value;
                }

                startIndex = i + 1;
                addition = false;
            }
        }

        if (addition) {
            String term = expression.substring(startIndex);
            double value = evaluate(term);
            total += value;
        } else {
            String term = expression.substring(startIndex);
            double value = evaluate(term);
            total -= value;
        }

        return total;
    }

    private static double convertDollarsToRubles(double dollars) {
        return Math.round(dollars * conversionRate * 100) / 100.0;
    }

    private static double convertRublesToDollars(double rubles) {
        return Math.round(rubles / conversionRate * 100) / 100.0;
    }


    private static void loadConversionRate() {
        try (BufferedReader reader = new BufferedReader(new FileReader("D:\\Testovoe_Modsen\\src\\com\\company\\config.txt"))) {
            String line = reader.readLine();
            conversionRate = Double.parseDouble(line);
        } catch (IOException e) {
            System.out.println("Error reading conversion rate from the configuration file.");
            System.exit(1);
        } catch (NumberFormatException e) {
            System.out.println("Invalid conversion rate format in the configuration file.");
            System.exit(1);
        }
    }
}
