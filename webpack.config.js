var webpack = require("webpack");
var path = require("path");
var srcDir = path.join(__dirname, 'src/main/frontend');
var destDir = path.join(__dirname, "target/classes/static");
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var IndexHtmlPlugin = require('indexhtml-webpack-plugin');
var cssExtractPlugin = new ExtractTextPlugin('css/[contenthash].css');
var glob = require('glob');
var entries = {
		// if an HTML file references JS, that JS file must be listed here until https://github.com/unbroken-dome/indexhtml-webpack-plugin/issues/2 is resolved
		'availability': './js/availability.js'
};
var plugins = [
//	new webpack.optimize.OccurenceOrderPlugin(),
	new webpack.optimize.DedupePlugin(),
//	new webpack.optimize.UglifyJsPlugin({
//	    compress: {
//	        warnings: false
//	    }
//	}),
	new webpack.optimize.LimitChunkCountPlugin({ maxChunks: 20 }),
	cssExtractPlugin
];
glob.sync('**/*.html', {cwd: srcDir}).forEach(function(item) { 
	entries[item] = ["./" + item];
	plugins.push(new IndexHtmlPlugin(item, item));
});
module.exports = {
	context: srcDir,
	bail: true,
	entry: entries,
	devtool: 'source-map', // must be 'source-map' or 'inline-source-map'
	output: {
		path: destDir,
		publicPath: "/",
		filename: "js/[name]-[chunkhash].js",
		chunkFilename: "js/[id]-[chunkhash].js",
		sourceMapFilename: "[file].map"
	},
	recordsOutputPath: path.join(destDir, "records.json"),
	module: {
		loaders: [
			{ test: /\.html$/,   loader: "html?root=.&attrs=link:href img:src" },
			{ test: /\.json$/,   loader: "json" },
			{ test: /\.css$/,    loader: cssExtractPlugin.extract('style', 'css?sourceMap') },
			{ test: /\.less$/,   loader: cssExtractPlugin.extract('style', 'css?sourceMap!less?sourceMap') },
			{ test: /\.(jpe?g|png|gif|svg)$/i,    loader: "file?name=[path][name]-[hash].[ext]!img" },
			{ test: /\.woff$/,   loader: "file?name=[path][name]-[hash].[ext]" },
			{ test: /\.eot$/,    loader: "file?name=[path][name]-[hash].[ext]" },
			{ test: /\.ttf$/,    loader: "file?name=[path][name]-[hash].[ext]" },
		],
		preLoaders: [
			{
				test: /\.js$/,
				include: pathToRegExp(srcDir),
				loader: "jshint"
			}
		]
	},
	plugins: plugins
};
function escapeRegExpString(str) { return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&"); }
function pathToRegExp(p) { return new RegExp("^" + escapeRegExpString(p)); }

