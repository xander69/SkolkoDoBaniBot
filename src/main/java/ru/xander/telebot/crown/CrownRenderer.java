package ru.xander.telebot.crown;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.util.StringUtils;
import ru.xander.telebot.dto.Fonts;
import ru.xander.telebot.util.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexander Shakhov
 */
public class CrownRenderer {

    private static final String DATASOURCE = "https://en.wikipedia.org/wiki/2019%E2%80%9320_Wuhan_coronavirus_outbreak_by_country_and_territory";
    private static final int TIMEOUT_MILLIS = 10000;
    private final ConcurrentHashMap<String, Image> flagsCache = new ConcurrentHashMap<>();

    public InputStream render() {
        try {
            Crown crown = extract();

            final int width = 376;
            final int height = 22 * (crown.getRegions().size() + 3) + 1;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            drawCrownTable(graphics, crown, width, height);

            graphics.dispose();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Cannot render forecast: " + e.getMessage(), e);
        }
    }

    private void drawCrownTable(Graphics2D graphics, Crown crown, int width, int height) {
        final int rowHeight = 22;
        final int col1 = 4;
        final int col2 = 154;
        final int col3 = 229;
        final int col4 = 304;

        graphics.setColor(new Color(0xf8, 0xf9, 0xfa));
        graphics.fill(new Rectangle(0, 0, width, height));

        graphics.setColor(new Color(0xea, 0xec, 0xf0));
        graphics.fill(new Rectangle(0, 0, width, rowHeight));
        graphics.fill(new Rectangle(0, height - rowHeight * 2, width, height));

        graphics.setColor(new Color(0xa2, 0xa9, 0xb1));
        for (int i = 0; i < (crown.getRegions().size() + 3); i++) {
            graphics.drawRect(0, i * rowHeight, width, (i + 1) * rowHeight);
        }
        graphics.drawRect(0, 0, width - 1, height - 1);
        graphics.drawRect(0, 0, 150, height - rowHeight - 1);
        graphics.drawRect(225, 0, 75, height - rowHeight - 1);

        graphics.setFont(Fonts.NEWS_CYCLE.getFont(14.0f));
        graphics.setColor(Color.BLACK);
        int item = 1;
        int totalConfirmed = 0;
        int totalDeaths = 0;
        int totalRecoveries = 0;
        for (Crown.Region region : crown.getRegions()) {
            graphics.drawString(region.getName(), col1, item * rowHeight + 17);
            graphics.drawImage(region.getFlag(), col2 - 30, item * rowHeight + 3, 23, 15, Color.WHITE, null);
            if (region.getConfirmed() != null) {
                graphics.drawString(String.valueOf(region.getConfirmed()), col2, item * rowHeight + 17);
                totalConfirmed += region.getConfirmed();
            }
            if (region.getDeaths() != null) {
                graphics.drawString(String.valueOf(region.getDeaths()), col3, item * rowHeight + 17);
                totalDeaths += region.getDeaths();
            }
            if (region.getRecoveries() != null) {
                graphics.drawString(String.valueOf(region.getRecoveries()), col4, item * rowHeight + 17);
                totalRecoveries += region.getRecoveries();
            }
            item++;
        }

        graphics.setFont(Fonts.NEWS_CYCLE.getMediumFont().deriveFont(Font.BOLD, 14.0f));

        graphics.drawString("Страна", col1, 17);
        graphics.drawString("Заражено", col2, 17);
        graphics.drawString("Смерти", col3, 17);
        graphics.drawString("Вылечено", col4, 17);

        graphics.drawString("Всего", col1, height - 6 - rowHeight);
        graphics.drawString(String.valueOf(totalConfirmed), col2, height - 6 - rowHeight);
        graphics.drawString(String.valueOf(totalDeaths), col3, height - 6 - rowHeight);
        graphics.drawString(String.valueOf(totalRecoveries), col4, height - 6 - rowHeight);

        double mortality = (totalDeaths / (double) totalConfirmed) * 100.0d;
        graphics.drawString(String.format("Смертность = %.2f%%", mortality) , col1, height - 6);
    }

    private Crown extract() throws IOException {
        Crown crown = new Crown();
        LinkedList<Crown.Region> regions = new LinkedList<>();
        Document document = Jsoup.parse(new URL(DATASOURCE), TIMEOUT_MILLIS);
        Element table = document.getElementsByAttributeValueStarting("class", "wikitable").first();
        for (Element tr : table.getElementsByTag("tr")) {
            if (StringUtils.isEmpty(tr.attr("class"))) {
                Crown.Region region = new Crown.Region();
                int item = 0;
                for (Element td : tr.getElementsByTag("td")) {
                    if (item == 0) {
                        region.setFlag(getFlag(td));
                        region.setName(td.text().replace(" ", Utils.EMPTY_STRING).trim());
                    } else if (item == 1) {
                        region.setConfirmed(parseInteger(td));
                    } else if (item == 2) {
                        region.setDeaths(parseInteger(td));
                    } else if (item == 3) {
                        region.setRecoveries(parseInteger(td));
                        break;
                    }
                    item++;
                }
                if (region.getName() != null) {
                    regions.add(region);
                }
            }
        }
        crown.setRegions(regions);
        return crown;
    }

    private Image getFlag(Element td) throws IOException {
        Element img = td.getElementsByTag("img").first();
        if (img != null) {
            String imgUrl = img.attr("src");
            if (imgUrl.startsWith("//")) {
                imgUrl = "https:" + imgUrl;
            }
            if (flagsCache.containsKey(imgUrl)) {
                return flagsCache.get(imgUrl);
            } else {
                Image flag = ImageIO.read(new URL(imgUrl).openStream());
                flagsCache.put(imgUrl, flag);
                return flag;
            }
        }
        return null;
    }

    private static Integer parseInteger(Element td) {
        String value = td.text().replace(",", Utils.EMPTY_STRING).trim();
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return Integer.parseInt(value);
    }

    public static void main(String[] args) throws IOException {
        IOUtils.copy(new CrownRenderer().render(), new FileOutputStream("d:\\Sources\\.temp\\crown.png"));
    }
}