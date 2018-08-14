package util;

import java.io.IOException;
import java.lang.reflect.Method;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class EnumAdapterFactory implements TypeAdapterFactory {
	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		Class<? super T> rawType = type.getRawType();
		if (rawType.isEnum()) {
			return new EnumTypeAdapter<>(type);
		}
		return null;
	}

	public class EnumTypeAdapter<T> extends TypeAdapter<T> {
		TypeToken<T> type;

		public EnumTypeAdapter(TypeToken<T> type) {
			this.type = type;
		}

		@SuppressWarnings("rawtypes")
		public void write(JsonWriter out, T value) throws IOException {
			if (value == null) {
				out.nullValue();
				return;
			}
			try {
				Method getValueMethod = type.getRawType().getMethod("getValue");
				out.value((Number) getValueMethod.invoke(value, new Object[] {}));

				return;
			} catch (Exception e) {
				// Not for production code. Exceptions must be either handled, or rethrown
			}

			out.value(((Enum) value).name());

		}

		@SuppressWarnings("unchecked")
		public T read(JsonReader in) {
			try {
				int intVal = Integer.parseInt(in.nextString());
				Method forValueMethod = type.getRawType().getMethod("forValue", int.class);

				return (T) forValueMethod.invoke(null, new Object[] { intVal });
			} catch (Exception e) {
				// Not for production code. Exceptions must be either handled, or rethrown
			}

			return null;
		}
	}
}
