package bwdm.boundaryValueAnalysisUnit;

import bwdm.informationStore.Node;
import bwdm.informationStore.IfNode;
import bwdm.Util;

import java.util.ArrayList;
import java.util.HashMap;


public class ExpectedOutputDataGenerator {


	private ArrayList<String> expectedOutputDataList;

	public ExpectedOutputDataGenerator(IfNode _root,
									   ArrayList<String> _parameters,
									   ArrayList<HashMap<String, Long>> _inputDataList) {
		expectedOutputDataList = new ArrayList();

		_inputDataList.forEach(inputData -> {
			extractExpectedOutputDataRecursively(_root, _parameters, inputData);
		});
	}

	public ArrayList<String> getExpectedOutputDataList() { return expectedOutputDataList; }

	//条件式は両辺のうち片方のみ変数が含まれているという制約付き
	void extractExpectedOutputDataRecursively(Node _node,
											  ArrayList<String> _parameters,
											  HashMap<String, Long> _inputData) {

		if(_node.isIfNode) { //IfNodeである場合
			HashMap<String, String> parsedCondition = makeParsedCondition(_node.getConditionOrReturnStr());
			long[] inputDataArray = new long[_parameters.size()];
			for(int i=0; i<_parameters.size(); i++) {
				inputDataArray[i] = _inputData.get(_parameters.get(i)).longValue();
			}

			//各条件式には一つの変数(parameter)しか登場しない
			_parameters.forEach(prm -> {
				//条件式中の変数とprmが一致したらinputDataを代入して真偽判定
				if(parsedCondition.get("right").equals(prm) || parsedCondition.get("left").equals(prm)) {
					boolean conditionJudgeResult = judge(parsedCondition, _inputData, prm);

					//判定結果がTRUEならばifNodeのtrueNodeに進んで再帰
					if( conditionJudgeResult ) {
						extractExpectedOutputDataRecursively(((IfNode)_node).conditionTrueNode, _parameters, _inputData);
					} else { //判定結果がFalseならばifNodeのfalseNodeに進んで再帰
						extractExpectedOutputDataRecursively(
								((IfNode)_node).conditionFalseNode,
								_parameters,
								_inputData
						);
					}
				}
			});
		} else { //returnNodeである場合
			String returnStr = _node.getConditionOrReturnStr();
			expectedOutputDataList.add(returnStr);
		}

	}

	public static HashMap makeParsedCondition(String _condition) {
		HashMap<String, String> parsedCondition = new HashMap();
		String operator = Util.getOperator(_condition);
		int indexOfoperator = _condition.indexOf(operator);

		parsedCondition.put("left", _condition.substring(0, indexOfoperator));
		parsedCondition.put("operator", operator);
		if(operator.equals("mod")) {
			int indexOfEqual = _condition.indexOf("=");
			parsedCondition.put("right", _condition.substring(indexOfoperator+3, indexOfEqual));
			parsedCondition.put("surplus", _condition.substring(indexOfEqual+1));
		} else {
			parsedCondition.put("right", _condition.substring(indexOfoperator + operator.length()));
		}
		return parsedCondition;
	}

	private boolean judge(HashMap<String, String> _parsedCondition,
						  HashMap<String, Long> _inputData,
						  String _parameter) {
		boolean result;

		if(Util.isNumber(_parsedCondition.get("left"))) { //右辺が変数
			if(_parsedCondition.get("operator").equals("mod")) {
				result = judgeMod(
						Long.valueOf(_parsedCondition.get("left")), //左辺：数字
						Long.valueOf(_inputData.get(_parameter)), //右辺：変数
						Long.valueOf(_parsedCondition.get("surplus"))
						);
			} else {
				result = judgeInequality(
						Long.valueOf(_parsedCondition.get("left")), //左辺：数字
						_parsedCondition.get("operator"),
						Long.valueOf(_inputData.get(_parameter)) //右辺：変数
				);
			}
		}
		else { //左辺が変数
			if(_parsedCondition.get("operator").equals("mod")) {
				result = judgeMod(
						Long.valueOf(_inputData.get(_parameter)), //左辺：変数
						Long.valueOf(_parsedCondition.get("right")), //右辺：数字
						Long.valueOf(_parsedCondition.get("surplus"))
				);
			} else {
				result = judgeInequality(
						Long.valueOf(_inputData.get(_parameter)), //左辺：変数
						_parsedCondition.get("operator"),
						Long.valueOf(_parsedCondition.get("right"))//右辺：数字
				);
			}

		}

		return result;
	}

	private boolean judgeMod(Long _leftHand, Long _rightHand, Long _surplus){
		return _leftHand % _rightHand == _surplus;
	}

	private boolean judgeInequality(Long _leftHand, String _operator, Long _rightHand){
		boolean returnBool ;
		if(_operator.equals("<")) {
			returnBool = _leftHand < _rightHand;
		}
		else if(_operator.equals(">")) {
			returnBool = _leftHand > _rightHand;
		}
		else if(_operator.equals("<=")) {
			returnBool = _leftHand <= _rightHand;
		}
		else if(_operator.equals(">=")) {
			returnBool = _leftHand >= _rightHand;
		}
		else {
			returnBool = true;
		}
		return returnBool;
	}

}