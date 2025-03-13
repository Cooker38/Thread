public class CalculatorModel {
    private double num1;
    private double num2;
    private double result;
    private char operator;

    public CalculatorModel() {
        this.num1 = 0;
        this.num2 = 0;
        this.result = 0;
        this.operator = '\0';
    }

    public double getNum1() {
        return num1;
    }

    public void setNum1(double num1) {
        this.num1 = num1;
    }

    public double getNum2() {
        return num2;
    }

    public void setNum2(double num2) {
        this.num2 = num2;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public char getOperator() {
        return operator;
    }

    public void setOperator(char operator) {
        this.operator = operator;
    }

    public void calculate() {
        switch (operator) {
            case '+':
                result = num1 + num2;
                break;
            case '-':
                result = num1 - num2;
                break;
            case '*':
                result = num1 * num2;
                break;
            case '/':
                if (num2 != 0) {
                    result = num1 / num2;
                } else {
                    throw new ArithmeticException("除数不能为零");
                }
                break;
            default:
                throw new IllegalArgumentException("无效的操作符");
        }
    }

    public void clear() {
        num1 = 0;
        num2 = 0;
        result = 0;
        operator = '\0';
    }

    public void negate() {
        if (operator == '\0') {
            num1 = -num1;
        } else {
            num2 = -num2;
        }
    }

    public void percentage() {
        if (operator == '\0') {
            num1 = num1 / 100;
        } else {
            num2 = num2 / 100;
        }
    }

    public void squareRoot() {
        if (operator == '\0') {
            if (num1 >= 0) {
                num1 = Math.sqrt(num1);
            } else {
                throw new ArithmeticException("负数不能开平方");
            }
        } else {
            if (num2 >= 0) {
                num2 = Math.sqrt(num2);
            } else {
                throw new ArithmeticException("负数不能开平方");
            }
        }
    }

    public void power() {
        if (operator == '\0') {
            num1 = Math.pow(num1, 2);
        } else {
            num2 = Math.pow(num2, 2);
        }
    }

    public void power(double exponent) {
        if (operator == '\0') {
            num1 = Math.pow(num1, exponent);
        } else {
            num2 = Math.pow(num2, exponent);
        }
    }

    public void nthRoot(double n) {
        if (operator == '\0') {
            if (num1 >= 0 || n % 2 == 1) {
                num1 = Math.pow(num1, 1.0 / n);
            } else {
                throw new ArithmeticException("负数不能开偶数次根");
            }
        } else {
            if (num2 >= 0 || n % 2 == 1) {
                num2 = Math.pow(num2, 1.0 / n);
            } else {
                throw new ArithmeticException("负数不能开偶数次根");
            }
        }
    }
}
