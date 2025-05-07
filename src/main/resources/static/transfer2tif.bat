cd C:\Program Files\ImageMagick-7.1.1-Q16-HDRI
magick.exe %1.jpg -profile C:\Users\17643\Desktop\JapanColor2001Coated.icc -profile C:\Users\17643\Desktop\sRGB2014.icc -strip C:\Users\17643\Desktop\tmp.jpg
D:
cd D:\Program Files\QGIS 3.28.12\bin
gdal_translate.exe -of GTiff -a_ullr 0 100 138.088 0 -a_srs EPSG:4326 C:\Users\17643\Desktop\tmp.jpg %2.tif
del C:\Users\17643\Desktop\tmp.jpg