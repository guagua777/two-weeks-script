package stone;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    //pat1是整形字面量， pat2是字符串字面量， pat3标识符
    // \s*是匹配多个空格
    // //.*用于匹配注释
    public static String regexPat
        = "\\s*((//.*)|([0-9]+)|(\"(\\\\\"|\\\\\\\\|\\\\n|[^\"])*\")"
          + "|[A-Z_a-z][A-Z_a-z0-9]*|==|<=|>=|&&|\\|\\||\\p{Punct})?";


//    //使用()括号括起来，视为一个完整的模式
//    public static String numberRegex = "|([0-9]+)|";
//    //为什么这个不用()括号括起来
//    //这里的标识符包括：普通标识符，== 等等
//    //\p{Punct} 匹配标点符号，+ - 属于标点符号
//    public static String identifierRegex = "|[A-Z_a-z][A-Z_a-z0-9]*|==|<=|>=|&&|\\|\\||\\p{Punct}";
//    //匹配注释
//    (//.*)
//    如果匹配的是标识符，则与第一个左括号(匹配
//    行首



    private Pattern pattern = Pattern.compile(regexPat);
    private ArrayList<Token> queue = new ArrayList<Token>();
    private boolean hasMore;
    private LineNumberReader reader;

    public Lexer(Reader r) {
        hasMore = true;
        reader = new LineNumberReader(r);
    }
    //从源代码头部获取单词
    public Token read() throws ParseException {
        if (fillQueue(0))
            return queue.remove(0);
        else
            return Token.EOF;
    }
    //预读
    public Token peek(int i) throws ParseException {
        if (fillQueue(i))
            return queue.get(i);
        else
            return Token.EOF; 
    }
    private boolean fillQueue(int i) throws ParseException {
        //如果该行单词个数不够，读取下一行
        while (i >= queue.size())
            if (hasMore)
                readLine();
            else
                return false;
        return true;
    }
    protected void readLine() throws ParseException {
        String line;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            throw new ParseException(e);
        }
        if (line == null) {
            hasMore = false;
            return;
        }
        int lineNo = reader.getLineNumber();
        Matcher matcher = pattern.matcher(line);
        //行首
        //设置这两个选项的值，用于设置边界
        matcher.useTransparentBounds(true).useAnchoringBounds(false);
        int pos = 0;
        int endPos = line.length();
        while (pos < endPos) {
            //设置matcher的范围
            matcher.region(pos, endPos);
            if (matcher.lookingAt()) {
                addToken(lineNo, matcher);
                pos = matcher.end();
            }
            else
                throw new ParseException("bad token at line " + lineNo);
        }
        //EOL是identifier token
        queue.add(new IdToken(lineNo, Token.EOL));
    }
    protected void addToken(int lineNo, Matcher matcher) {
        String m = matcher.group(1);
        if (m != null) // if not a space
            if (matcher.group(2) == null) { // if not a comment
                Token token;
                if (matcher.group(3) != null)
                    token = new NumToken(lineNo, Integer.parseInt(m));
                else if (matcher.group(4) != null)
                    token = new StrToken(lineNo, toStringLiteral(m));
                else
                    token = new IdToken(lineNo, m);
                queue.add(token);
            }
    }
    protected String toStringLiteral(String s) {
        StringBuilder sb = new StringBuilder();
        int len = s.length() - 1;
        for (int i = 1; i < len; i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < len) {
                int c2 = s.charAt(i + 1);
                if (c2 == '"' || c2 == '\\')
                    c = s.charAt(++i);
                else if (c2 == 'n') {
                    ++i;
                    c = '\n';
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    protected static class NumToken extends Token {
        private int value;

        protected NumToken(int line, int v) {
            super(line);
            value = v;
        }
        public boolean isNumber() { return true; }
        public String getText() { return Integer.toString(value); }
        public int getNumber() { return value; }
    }

    protected static class IdToken extends Token {
        private String text; 
        protected IdToken(int line, String id) {
            super(line);
            text = id;
        }
        public boolean isIdentifier() { return true; }
        public String getText() { return text; }
    }

    protected static class StrToken extends Token {
        private String literal;
        StrToken(int line, String str) {
            super(line);
            literal = str;
        }
        public boolean isString() { return true; }
        public String getText() { return literal; }
    }
}
