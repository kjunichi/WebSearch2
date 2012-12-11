package com.cocolog_nifty.kjunichi;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class GetUrlFromTwitter {

	private String getTwitterPage() {
		String twitterPageUrl = "http://twitter.com/statuses/public_timeline.rss";
		String text = null;
		try {
			WebConversation wc = new WebConversation();
			//wc.setHeaderField("Referer","");

			// �y�[�W��\������
			WebResponse homePage = wc.getResponse(twitterPageUrl);
			// if (homePage.getContentType().indexOf("text") < 0) {
			// // �e�L�X�g�ȊO�͑ΏۊO
			// return null;
			// }

			// �Ώۂ̃y�[�W���e�L�X�g�Ŏ��o��
			text = homePage.getText();
			
			// �s�v�ȃe�L�X�g���폜����
			

		} catch (Throwable e) {
			// �e�N���C�A���g�Ŕ��������G���[�͌Ăяo�����ɒʒm���Ȃ��B
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return text;
	}

	public void execute() {
		// Twitter��RSS�y�[�W�ɃA�N�Z�X����B
		String contents = getTwitterPage();
		System.out.println(contents);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new GetUrlFromTwitter().execute();
	}
}
