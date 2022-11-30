package com.scanner.document.Scanning;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.scanner.document.Model.Document;
import com.scanner.document.Model.OcrDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Converter {

    @TypeConverter
    public static String fromList(ArrayList<String> list){
        String res = "";
        for(String str : list){
            res += str + ",";
        }

        return res;
    }

    @TypeConverter
    public static ArrayList<String> toList(String formattedString){
        List<String> list = Arrays.asList(formattedString.split("\\s*,\\s*"));
        return new ArrayList<>(list);
    }

    @TypeConverter
    public static String fromDocument(Document document){
        return new Gson().toJson(document);
    }

    @TypeConverter
    public static Document toDocument(String formattedDoc){
        return new Gson().fromJson(formattedDoc, Document.class);
    }

    @TypeConverter
    public static String fromOcrDocument(OcrDocument ocrDocument){
        return new Gson().toJson(ocrDocument);
    }

    @TypeConverter
    public static OcrDocument toOcrDocument(String formattedDoc){
        return new Gson().fromJson(formattedDoc, OcrDocument.class);
    }
}
