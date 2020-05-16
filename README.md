# webApi
俄罗斯交易软件提供商迈达克（MetaQuotes）推进的MT5交易平台，是服务世界各地主要的外汇交易商和交易所等机构的主流外汇交易平台。目前它提供的MetaTrader5 Web API 是基于文本的开放协议,旨在集成 MetaTrader 5 平台与公司的网络资源和其他服务。使用 Web API，您可以为交易员安排构件区域、执行报价的在线播送以及就在您的网站上组织交易账户的开立。

但是目前官方只提供.net和php两个版本的实现(https://support.metaquotes.net/zh/docs/mt5/api/webapi), 有人在GitHub上写了一个python的版本(https://github.com/roma-guru/mt5webapi) ，因此自己产生了使用java去实现的想法。这个项目是基于springboot和netty去实现的，由于写得比较仓促，有不足之处，望与有兴趣的朋友共同优化。
