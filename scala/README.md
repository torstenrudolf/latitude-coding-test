# Fixed-Width to CSV converter

The main class first generates a fixed-width file encoded in `windows-1252` 
and then reads  the file back in and converts it to a `utf-8` encoded CSV file.

According to the spec the column names are `"f1, f2, f3, f4, f5, f6, f7, f8, f9, f10"` and 
the column widths are `"3,12,3,2,13,1,10,13,3,13"`.

The scala code uses `fs2` to stream the files and could handle very large files at constant memory.

## RUN

In order to run the application, simply write `sbt run`.

`sbt test` runs all tests.

`sbt docker` to create a docker image.

If you don't want to build, you can also just run 
```
docker run -v `pwd`/data:/data --env DATA_DIR=/data trudolf/latitude-coding-test
```
This will output the two files into ``` `pwd`/data/ ```