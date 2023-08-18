/*
Karuah Chess is a chess playing program
Copyright (C) 2020-2023 Karuah Software

Karuah Chess is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Karuah Chess is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package purpletreesoftware.karuahchess.common;

import java.io.*;

public class HelperJava {

    /**
     * Serialises a class to xml
     * @param dataToSerialize
     * @return
     */
    public static <T> byte[] Serialize(T dataToSerialize) throws IOException
    {
        byte[] stream;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(dataToSerialize);
        stream = baos.toByteArray();
        return stream;
    }

    /**
     * Deseerialises an xml string
     * @param xmlByte
     * @return
     */
    public static <T> T Deserialize(byte[] xmlByte) throws IOException, ClassNotFoundException
    {
        T obj = null;

        if (xmlByte != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlByte);
            ObjectInputStream ois = new ObjectInputStream(bais);
            obj =  Cast(ois.readObject());
        }

        return obj;
    }

    @SuppressWarnings("unchecked")
    public static <T> T Cast(Object obj) {
        return (T) obj;
    }

}
