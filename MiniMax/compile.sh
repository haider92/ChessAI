javac -cp "*" *.java 
jar cf0 MiniMax.jar *.class
java -cp "*" org.w2mind.toolkit.Main -mind MiniMax -world w2m.ChessWorldG -g
