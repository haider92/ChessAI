javac -cp "*" *.java 
jar cf0 AlphaBeta.jar *.class
java -cp "*" org.w2mind.toolkit.Main -mind AlphaBeta -world w2m.ChessWorldG -g
