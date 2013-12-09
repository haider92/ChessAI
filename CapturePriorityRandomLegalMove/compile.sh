javac -cp "*" *.java 
jar cf0 CapturePriorityRandomLegalMove.jar *.class
java -cp "*" org.w2mind.toolkit.Main -mind CapturePriorityRandomLegalMove -world w2m.ChessWorldG -g
