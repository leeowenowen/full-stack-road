import FindKey

keys_string = FindKey.FindKey("Main.class")
keys = keys_string.split('####')
for key in keys:
  if len(key) > 0:
    print key
