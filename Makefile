all : PicOrg.class


PicOrg.class : PicOrg.java
	javac PicOrg.java


run : 
	java PicOrg


