package com.example.eb_meter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PdfUtility {
    private static final String TAG = PdfUtility.class.getSimpleName();
    private static final Font FONT_TITLE = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static final Font FONT_SUBTITLE = new Font(Font.FontFamily.TIMES_ROMAN, 10, Font.NORMAL);
    private static final Font FONT_CELL = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
    private static final Font FONT_COLUMN = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.NORMAL);

    public interface OnDocumentClose {
        void onPdfDocumentClose(File file);
    }

    static void createPdf(@NonNull Context mContext, OnDocumentClose mCallback, List<String[]> items, @NonNull String filePath, boolean isPortrait) throws InterruptedException {
        if(filePath.equals(""))
        {
            throw new NullPointerException("PDF File Name can't be null or blank. PDF File can't be created");
        }

        File file = new File(filePath);

        //error handling if file path doesn't exist
        if(file.exists())
        {
            file.delete();
            Thread.sleep(50);
        }

        //creates a pdf document
        try {
            Document document = new Document();

            //set pdf margins
            document.setMargins(24f, 24f, 32f, 32f);

            //creates an A4 sized pdf that is keep as portrait
            document.setPageSize(isPortrait ? PageSize.A4 : PageSize.A4.rotate());

            //file writer
            PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            pdfWriter.setFullCompression();
            pdfWriter.setPageEvent(new PageNumeration());

            //open file for write
            document.open();

            //sets metadata for pdf
            setMetaData(document);

            //header for pdf
            addHeader(mContext, document);

            //adds 3 empty lines
            addEmptyLine(document, 3);

            //main function that adds data
            document.add(createDataTable(items));

            addEmptyLine(document, 2);

            document.close();
            pdfWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"Error While Closing pdfWriter : "+ e);
        }

        if(mCallback!=null)
        {
            mCallback.onPdfDocumentClose(file);
        }
    }

    private static  void addEmptyLine(Document document, int number) throws DocumentException
    {
        for (int i = 0; i < number; i++)
        {
            document.add(new Paragraph(" "));
        }
    }

    private static void setMetaData(Document document)
    {
        document.addCreationDate();
        document.addAuthor( "Electricity board");
        document.addCreator("Electricity board e-bill");
        document.addHeader("CEB Corporation","Electricity Board");
    }

    private static void addHeader(Context mContext, Document document) throws Exception
    {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2,7,2});
        table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell cell;
        {
            /*LEFT TOP LOGO*/
            Drawable d= ContextCompat.getDrawable(mContext, R.drawable.pdf_logo);
            Bitmap bmp=((BitmapDrawable) d).getBitmap();
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG,100,stream);

            Image logo=Image.getInstance(stream.toByteArray());
            logo.setWidthPercentage(80);
            logo.scaleToFit(105,55);

            cell = new PdfPCell(logo);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setUseAscender(true);
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(2f);
            table.addCell(cell);
        }

        {
            /*MIDDLE TEXT*/
            cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(8f);
            cell.setUseAscender(true);

            Paragraph temp = new Paragraph("CEB Electricity Bill" ,FONT_TITLE);
            temp.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(temp);

            temp = new Paragraph("Customer name: Bevan\nAccount number: 0123456789\nBilling period: 01/10/2022 - 31/10/2022\nPayment Due Date: 14/11/2022" ,FONT_SUBTITLE);
            temp.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(temp);

            table.addCell(cell);
        }
        /* RIGHT TOP LOGO*/
        {
            PdfPTable logoTable=new PdfPTable(1);
            logoTable.setWidthPercentage(100);
            logoTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            logoTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            logoTable.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);

            Drawable drawable=ContextCompat.getDrawable(mContext, R.drawable.pdf_logo2);
            Bitmap bmp =((BitmapDrawable)drawable).getBitmap();

            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG,100,stream);
            Image logo=Image.getInstance(stream.toByteArray());
            logo.setWidthPercentage(80);
            logo.scaleToFit(38,38);

            PdfPCell logoCell = new PdfPCell(logo);
            logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            logoCell.setBorder(PdfPCell.NO_BORDER);

            logoTable.addCell(logoCell);

            cell = new PdfPCell(logoTable);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setUseAscender(true);
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.setPadding(2f);
            table.addCell(cell);
        }

        document.add(table);
    }

    private static PdfPTable createDataTable(List<String[]> dataTable) throws DocumentException
    {
        PdfPTable table1 = new PdfPTable(3);
        table1.setWidthPercentage(100);
        table1.setWidths(new float[]{2f,2f,2f});
        table1.setHeaderRows(1);
        table1.getDefaultCell().setVerticalAlignment(Element.ALIGN_CENTER);
        table1.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell cell;
        {
            cell = new PdfPCell(new Phrase("Rooms", FONT_COLUMN));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(4f);
            table1.addCell(cell);

            cell = new PdfPCell(new Phrase("Units Consumed/kWh", FONT_COLUMN));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(4f);
            table1.addCell(cell);

            cell = new PdfPCell(new Phrase("Charges/Rs.", FONT_COLUMN));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(4f);
            table1.addCell(cell);
        }

        float top_bottom_Padding = 8f;
        float left_right_Padding = 4f;
        boolean alternate = false;

        BaseColor lt_gray = new BaseColor(221,221,221); //#DDDDDD
        BaseColor cell_color;

        //finds the number of rows
        int size = dataTable.size();

        for (int i = 0; i < size; i++)
        {
            cell_color = alternate ? lt_gray : BaseColor.WHITE;
            String[] temp = dataTable.get(i);

            //adds item to Rooms column
            cell = new PdfPCell(new Phrase(temp[0], FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingLeft(left_right_Padding);
            cell.setPaddingRight(left_right_Padding);
            cell.setPaddingTop(top_bottom_Padding);
            cell.setPaddingBottom(top_bottom_Padding);
            cell.setBackgroundColor(cell_color);
            table1.addCell(cell);

            //adds item to Units consumed column
            cell = new PdfPCell(new Phrase(temp[1], FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingLeft(left_right_Padding);
            cell.setPaddingRight(left_right_Padding);
            cell.setPaddingTop(top_bottom_Padding);
            cell.setPaddingBottom(top_bottom_Padding);
            cell.setBackgroundColor(cell_color);
            table1.addCell(cell);

            //adds item to Charges column
            cell = new PdfPCell(new Phrase(temp[2], FONT_CELL));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPaddingLeft(left_right_Padding);
            cell.setPaddingRight(left_right_Padding);
            cell.setPaddingTop(top_bottom_Padding);
            cell.setPaddingBottom(top_bottom_Padding);
            cell.setBackgroundColor(cell_color);
            table1.addCell(cell);

            alternate = !alternate;
        }

        return table1;
    }

}
