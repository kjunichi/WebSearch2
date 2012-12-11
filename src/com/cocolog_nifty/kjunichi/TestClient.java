package com.cocolog_nifty.kjunichi;

import java.util.List;

import com.meterware.httpunit.HttpNotFoundException;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

import sun.misc.Cleaner;

public class TestClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://www.hellohiro.com/regex.htm";

		try {
			WebConversation wc = new WebConversation();
			//wc.setHeaderField("Referer","");

			// �y�[�W��\������
			WebResponse homePage = wc.getResponse(url);
			if (homePage.getContentType().indexOf("text") < 0) {
				// �e�L�X�g�ȊO�͑ΏۊO
				return;
			}

			// �Ώۂ̃y�[�W���e�L�X�g�Ŏ��o��
			String text = homePage.getText();

			// �y�[�W�̉��
			// �y�[�W����wiki�̃^�C�g�����܂܂�邩���`�F�b�N
			List<String> keywords = Util.getWikiTitles();
			for (String keyword : keywords) {
				if (text.indexOf(keyword) > -1) {
					// ��v�������X�g����v�L�[���[�h���X�g�̒ǉ�
					System.out.println(keyword + " : ����");
				} else {
					System.out.println(keyword + " : �Ȃ�");
				}
			}
		} catch (HttpNotFoundException hnfe) {
			// 404�͖����B
			hnfe.printStackTrace();
		} catch (Throwable e) {
			// �e�N���C�A���g�Ŕ��������G���[�͌Ăяo�����ɒʒm���Ȃ��B
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

}
