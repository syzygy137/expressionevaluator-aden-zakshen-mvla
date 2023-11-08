// TODO: Auto-generated Javadoc
public class ExpressionEvaluator {
	// These are the required error strings for that MUST be returned on the appropriate error 
	// for the JUnit tests to pass. DO NOT CHANGE!!!!!
	private static final String PAREN_ERROR = "Paren Error: ";
	private static final String OP_ERROR = "Op Error: ";
	private static final String DATA_ERROR = "Data Error: ";
	private static final String DIV0_ERROR = "Div0 Error: ";

	// The placeholder for the two stacks

	private GenericStack<Double> dataStack;
	private GenericStack<String>  operStack;
	
	/**
	 * Returns true if the first string has higher precedence
	 *
	 * @param str1 and str2
	 * @return whether or not str1 has precedence over str2
	 */
	private boolean isHigherPrecedence(String str1, String str2) {
		if (str1.equals("*") || str1.equals("/")) {
			if (str2.equals("(")) {
				return false;
			}
			return true;
		}
		if (str1.equals("+") || str1.equals("-")) {
			if (str2.equals("(") || str2.equals("*") || str2.equals("/")) {
				return false;
			}
			return true;
		}
		if (str2.equals(")")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Convert to tokens. Takes a string and splits it into tokens that
	 * are either operators or data. This is where you should convert 
	 * implicit multiplication to explicit multiplication. It is also a candidate
	 * for recognizing negative numbers, and then including that negative sign
	 * as part of the appropriate data token.
	 *
	 * @param str the str
	 * @return the string[] of tokens
	 */
	private String[] convertToTokens(String str) {
		str = str.replaceAll("^(\\s*)\\-([\\d\\.])", "$1NEG$2");
		str = str.replaceAll("([\\+\\-\\*\\/\\(]\\s*)\\-([\\d\\.])", "$1 NEG$2");
		str = str.replaceAll("([\\+\\-\\*\\/\\(\\)])", " $1 ");
		str = str.trim();
		str = str.replaceAll("\\)\\s+([\\d\\(\\.])", ") * $1");
		str = str.replaceAll("([\\d\\.])\\s+\\(", "$1 * (");
		str = str.replaceAll("(^\\s*|[\\(\\+\\-\\/\\*])\\s*\\-\\s+(\\()", "$1 = $2");
		str = sub(str);
		str = str.trim();
		String[] tokens = str.split("\\s+");
		tokens = removeNEG(tokens);
		return tokens;
	}
	
	/**
	 * Removes all = signs with implicit multiplication
	 *
	 * @param String str
	 * @return the updated String str
	 */
	private String sub(String str) {
		int parenCount = 0;
		boolean parenFound = false;
		while (str.contains("=")) {
			int startIndex = str.indexOf("=");
			int endIndex = 14;
			System.out.println(str);
			parenCount = 0;
			parenFound = false;
			for (int i = startIndex; i < str.length(); i++) {
				if (str.charAt(i) == '(') {
					parenCount++;
					parenFound = true;
				}
				if (str.charAt(i) == ')') {
					parenCount--;
				}
				if (parenFound && parenCount == 0) {
					endIndex = i;
					break;
				}
			}
			str = str.substring(0, startIndex) + "( NEG1 * " + str.substring(startIndex + 1, endIndex) + " ) " + str.substring(endIndex, str.length());
		}
		return str;
	}
	
	/**
	 * Removes NEG and replaces it with a negative sign
	 *
	 * @param String[] of tokens
	 * @return new String[] of tokens
	 */
	private String[] removeNEG(String[] tokens) {
		String[] removed = new String[tokens.length];
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (token.length() >= 3 && token.substring(0, 3).equals("NEG")) {
				token = token.replaceAll("NEG", "-");		
			}
			removed[i] = token;
		}
		return removed;
	}
	
	
	/**
	 * Does error checking for DATA_ERROR, OP_ERROR, and PAREN_ERROR before attempting to evaluate the expression
	 *
	 * @param String[] of tokens
	 * @return the error, or an empty string
	 */
	private String containsError(String[] tokens) {
		int parenCount = 0;
		if (tokens[0].matches("[\\+\\/\\*]") || tokens[tokens.length - 1].matches("[\\+\\/\\*\\-]"))
			return OP_ERROR;
		for (int i = 0; i < tokens.length; i++) {
			String err = dataOppErrors(tokens, i);
			if (err != "")
				return err;
			if (tokens[i].equals("(")) {
				if (i == tokens.length - 1 || tokens[i + 1].matches("[\\+\\-\\*\\/\\)]"))
					return PAREN_ERROR;
				parenCount++;
			}
			if (tokens[i].equals(")"))
				parenCount--;
			if (parenCount < 0)
				return PAREN_ERROR;
		}
		if (parenCount != 0)
			return PAREN_ERROR;
		return "";
	}
	
	
	/**
	 * Does error checking for DATA_ERROR and OP_ERROR as a helper method for containsError
	 *
	 * @param String[] of tokens, index i
	 * @return the error, or an empty string
	 */
	private String dataOppErrors(String[] tokens, int i) {
		String token = tokens[i];
		String next = "DNE";
		String next2 = "DNE";
		if (i < tokens.length - 1) {
			if (i < tokens.length - 2)
				next2 = tokens[i + 2];
			next = tokens[i + 1];
		}
		if (!token.matches("[\\+\\-\\*\\/\\)\\(]")) {
			try {
				Double.parseDouble(token);
			} catch (NumberFormatException e) {
				return DATA_ERROR;
			}
			if (!token.matches("[\\d.-]+"))
				return DATA_ERROR;
			if (!next.equals("DNE") && !next.matches("[\\+\\-\\*\\/\\)\\(]"))
				return DATA_ERROR;
		}
		if (!next.equals("DNE")) {
			if (!nextExists(token, next, next2).equals("")) {
				return nextExists(token, next, next2);
			}
		}
		return "";
	}
	
	/**
	 * Does error checking for consecutive operations
	 *
	 * @param Strings: token, next token, token after next token
	 * @return the error, or an empty string
	 */
	private String nextExists(String token, String next, String next2) {
		if (token.matches("[\\+\\*\\/\\-]")) {
			if (next.matches("[\\+\\*\\/\\)]"))
				return OP_ERROR;
		}
		if (next.equals("-")) {
			if (token.matches("[\\/\\*]"))
				return OP_ERROR;
		}
		if (token.equals("(") && next.matches("[\\+\\*\\/\\-]"))
			return OP_ERROR;
		if (token.matches("[\\+\\-\\*\\/]") && next.matches("[\\+\\-\\*\\/]") && next2.matches("[\\+\\-\\*\\/]")) 
			return OP_ERROR;
		return "";
	}
	
	/**
	 * Evaluate expression. This is it, the big Kahuna....
	 * It is going to be called by the GUI (or the JUnit tester),
	 * and:
	 * a) convert the string to tokens. This should detect and handle
	 *    implicit multiplication:
	 *    -- examples: ")   (" or "9(" or ")3" -- 
	 *    and negation:
	 *    -- examples: "7 + -9" or "-.9" or "-3(" but not ")-3"
	 * b) if conversion successful, perform static error checking
	 *    - Paren Errors
	 *    - Op Errors 
	 *    - Data Errors
	 * c) if static error checking is successful:
	 *    - evaluate the expression, catching any runtime errors.
	 *      For the purpose of this project, the only runtime errors are 
	 *      divide-by-0 errors.
	 *
	 * @param str the str
	 * @return the string
	 */
	protected String evaluateExpression(String str) {
        dataStack =  new GenericStack<Double>();
		operStack =  new GenericStack<String>();
		String[] tokens = convertToTokens(str);
		
		if (containsError(tokens) != "") {
			System.out.println(containsError(tokens));
			return containsError(tokens);
		}
		
		for (String token : tokens) {
			if (!token.matches("[\\+\\-\\*\\/\\(\\)]")) {
				dataStack.push(Double.parseDouble(token));
			} else {
				if (!executeOps(token).equals("")) {
					return DIV0_ERROR;
				}
				if (!token.equals(")"))
					operStack.push(token);
			}
		}
		if (!executeOps("+").equals("")) {
			return DIV0_ERROR;
		}
		return str + " = " + Double.toString(dataStack.pop());
	}
	
	/**
	 * Executes operations until more operation tokens are needed
	 *
	 * @param token
	 * @return DIV0_ERROR or an empty string
	 */
	private String executeOps(String token) {
		if (operStack.empty())
			return "";
		while (isHigherPrecedence(operStack.peek(), token)) {
			if (operStack.peek().equals("(") && token.equals(")")) {
				operStack.pop();
				break;
			} else {
				if (!executeTOS().equals("")) {
					return DIV0_ERROR;
				}
			}
			if (operStack.empty())
				break;
		}
		return "";
	}
	
	/**
	 * Executes the operation at the top of stack
	 *
	 * @param None
	 * @return DIV0_ERROR or an empty string
	 */
	private String executeTOS() {
		String op = operStack.pop();
		double a = dataStack.pop();
		double b = dataStack.pop();
		if (op.equals("+")) {
			dataStack.push(b + a);
		}
		if (op.equals("-")) {
			dataStack.push(b - a);
		}
		if (op.equals("*")) {
			dataStack.push(b * a);
		}
		if (op.equals("/")) {
			
			if (a == 0) {
				return DIV0_ERROR;
			}
			dataStack.push(b / a);
		}
		return "";
	}
	

}
