package odoo.controls;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.odoo.util.StringUtils;

public class OWebTextView extends TextView {

	private Context mContext;
	/** The display metrics. */
	private DisplayMetrics mMetrics = null;

	/** The scale factor. */
	private Float mScaleFactor = 0F;
	private Spanned htmlSpan = null;
	private String originalContent = null;
	private Boolean trimedContent = false;

	public OWebTextView(Context context) {
		this(context, null, 0);
	}

	public OWebTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OWebTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mMetrics = getResources().getDisplayMetrics();
		mScaleFactor = mMetrics.density;
		int padding = (int) (10 * mScaleFactor);
		setPadding(padding, padding, padding, 0);
		setClickable(true);
		setMovementMethod(new ScrollingMovementMethod());
		setMovementMethod(LinkMovementMethod.getInstance());
	}

	@Override
	public void setTextAppearance(Context context, int resid) {
		super.setTextAppearance(context, resid);
		setTypeface(OControlHelper.lightFont());
	}

	public void setHtmlContent(String content) {
		URLImageParser p = new URLImageParser(mContext, this);
		originalContent = content;
		htmlSpan = Html.fromHtml(content, p, null);
		setText(htmlSpan);
	}

	public void setHtmlSpanContent(Spanned content) {
		setText(content);
	}

	public Spanned getHtmlSpan() {
		return htmlSpan;
	}

	public void trimContent(int lines) {
		if (lines > 0) {
			StringBuffer newContent = new StringBuffer();
			int start = 0;
			int end = (originalContent.length() > 200) ? 200 : originalContent
					.length();
			newContent.append(StringUtils.htmlToString(originalContent)
					.substring(start, end));
			newContent.append("...");
			trimedContent = true;
			setHtmlContent(newContent.toString());
		} else {
			trimedContent = false;
		}
	}

	public boolean trimedContent() {
		return trimedContent;
	}

	@SuppressWarnings("deprecation")
	public class URLDrawable extends BitmapDrawable {
		protected Drawable drawable;

		@Override
		public void draw(Canvas canvas) {
			if (drawable != null) {
				drawable.draw(canvas);
			}
		}
	}

	public class URLImageParser implements ImageGetter {
		TextView container;
		URLDrawable urlDrawable;
		Context mContext;

		public URLImageParser(Context context, TextView textView) {
			container = textView;
			mContext = context;
		}

		public Drawable getDrawable(String source) {
			urlDrawable = new URLDrawable();

			ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(
					urlDrawable, mContext.getResources());

			asyncTask.execute(source);

			return urlDrawable;
		}

		public class ImageGetterAsyncTask extends
				AsyncTask<String, Void, Drawable> {
			URLDrawable urlDrawable;
			Resources res = null;

			public ImageGetterAsyncTask(URLDrawable d, Resources resource) {
				this.urlDrawable = d;
				res = resource;
			}

			@Override
			protected Drawable doInBackground(String... params) {
				String source = params[0];
				return fetchDrawable(source);
			}

			@Override
			protected void onPostExecute(Drawable result) {
				if (result != null) {
					float multiplier = (float) 200
							/ (float) result.getIntrinsicWidth();
					int width = (int) (result.getIntrinsicWidth() * multiplier);
					int height = (int) (result.getIntrinsicHeight() * multiplier);
					urlDrawable.setBounds(new Rect(0, 0, width, height));
					urlDrawable.drawable = result;
					URLImageParser.this.container.invalidate();
					URLImageParser.this.container
							.setHeight((URLImageParser.this.container
									.getHeight() + result.getIntrinsicHeight()));
				}
			}

			public Drawable fetchDrawable(String urlString) {
				try {
					InputStream is = fetch(urlString);
					int multiplier = 10;
					Drawable drawable = Drawable.createFromStream(is, "src");
					drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth()
							* multiplier, 0 + drawable.getIntrinsicHeight()
							* multiplier);
					return drawable;
				} catch (Exception e) {
					return null;
				}
			}

			private InputStream fetch(String urlString)
					throws MalformedURLException, IOException {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet request = new HttpGet(urlString);
				HttpResponse response = httpClient.execute(request);
				return response.getEntity().getContent();
			}
		}
	}
}
