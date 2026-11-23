/**
 * 应用级配置
 *
 * 运行时挂载到 getApp().globalData.config（见 common/appInit.js），
 * 页面统一通过 getApp().globalData.config.xxx 读取。
 *
 * 注意：后端服务地址不在本文件，见 common/config.js 的 BASE_URL。
 */
export default {
	"h5": {
		// H5 端网页托管的域名，用于生成分享链接与下载引导页，上线前需填自己的域名
		"url": "",
		"openApp": { // 如不需要 H5 端悬浮下载条，直接移除本节点即可
			// //点击悬浮下载栏后打开的网页链接
			// "openUrl": '/#/pages/ucenter/invite/invite',
			// //左侧显示的应用名称
			// "appname": 'ERP',
			// //应用的图标
			// "logo": './static/logo.png',
		}
	},
	//关于应用，pages/ucenter/about/about.vue 按此渲染
	"about": {
		//应用名称
		"appName": "ERP",
		//应用logo
		"logo": "/static/logo.png",
		//公司名称
		"company": "",
		//口号
		"slogan": "",
		//应用的链接，用于分享到第三方平台和生成关于我们页的二维码
		"download": "",
		//version
		"version": "1.0.0" //用于非app端显示，app端自动获取
	},
	"download": { //用于生成二合一下载页面
		"ios": "",
		"android": ""
	},
	//用于打开应用市场评分界面
	"marketId": {
		"ios": "",
		"android": ""
	},
	//配置多语言国际化。i18n为英文单词 internationalization的首末字符i和n，18为中间的字符数 是“国际化”的简称
	"i18n": {
		"enable": false //默认关闭，国际化。如果你想使用国际化相关功能，请改为true
	}
}
