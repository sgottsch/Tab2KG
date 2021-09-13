# This is an edited version of https://github.com/minhptx/iswc-2016-semantic-labeling, which was edited to use it as a baseline for Tab2KG (https://github.com/sgottsch/Tab2KG).

__author__ = 'alse'


def balance_result(num1, num2, is_num, result):
    if (num1 + num2 == 0 and not is_num) or (num1 + num2 == 2 and is_num):
        return 0
    if is_num:
        return (1 - num1) * (1 - num2) * result / (2 - num1 - num2)
    else:
        return num1 * num2 * result / (num1 + num2)
