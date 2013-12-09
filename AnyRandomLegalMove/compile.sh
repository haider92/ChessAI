javac -cp "*" *.java 
jar cf0 AnyRandomLegalMove.jar *.class
java -cp "*" org.w2mind.toolkit.Main -mind AnyRandomLegalMove -world w2m.ChessWorldG -g
