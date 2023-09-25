import requests
from food import average_nutrition


def nutrition_info_from_label(label, base_url):
    nutrition_info = average_nutrition[label]
    nutrition_info['mealname'] = label.replace('_', ' ').title()
    nutrition_info['mealimgurl'] = base_url + f"imgs/{label}.png"
    return nutrition_info

def get_recipes(dish_name, max_calories):
    recipes_data = __call_low_carb_recipes(dish_name)
    return __parse_recipe_json(recipes_data, max_calories)


def __call_low_carb_recipes(dish_name):
    url = "https://edamam-recipe-search.p.rapidapi.com/api/recipes/v2"

    querystring = {
        "type":"any",
        "q":dish_name.replace('_', ' '),
        "diet[0]":"Low-Fat",
        "diet[1]":"Low-Carb"
    }

    headers = {
        "Accept-Language": "en",
        "X-RapidAPI-Key": "<Your API Key>",
        "X-RapidAPI-Host": "edamam-recipe-search.p.rapidapi.com"
    }

    response = requests.get(url, headers=headers, params=querystring)

    return response.json()


def __parse_recipe_json(data, max_calories):
    recipes = []

    for recipe in data['hits']:
        recipe_data = recipe['recipe']
        calories = (recipe_data["calories"] / recipe_data['totalWeight']) * 100
        if max_calories > calories:
            recipe_info = {
                "name": recipe_data["label"],
                "duration": int(recipe_data["totalTime"]), 
                "calories": int(calories),
                "carbs": int((recipe_data["totalNutrients"]["CHOCDF"]["quantity"] / recipe_data['totalWeight']) * 100),
                "protein": int((recipe_data["totalNutrients"]["PROCNT"]["quantity"] / recipe_data['totalWeight']) * 100), 
                "fat": int((recipe_data["totalNutrients"]["FAT"]["quantity"] / recipe_data['totalWeight']) * 100), 
                "ingredients": recipe_data["ingredientLines"],
                "url": recipe_data["url"],
                "image_url": recipe_data["images"]["REGULAR"]["url"],
            }
            recipes.append(recipe_info)

    return recipes